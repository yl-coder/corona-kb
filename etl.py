import configparser
from datetime import datetime
import pandas as pd
import configparser
import subprocess
from datetime import datetime
import os
import requests
import urllib
import pyspark.sql.functions as F
from pyspark.sql import SparkSession
from pyspark.sql.functions import udf, col,unix_timestamp
from pyspark.sql.functions import year, month, dayofmonth, hour, weekofyear, date_format
from pyspark.sql.types import DateType, TimestampType, IntegerType,BooleanType,StringType
import uuid
from pyspark.sql.functions import udf, lit

config = configparser.ConfigParser()
config.read('dl.cfg')

os.environ['AWS_ACCESS_KEY_ID']=config.get("KEYS", "AWS_ACCESS_KEY_ID")
os.environ['AWS_SECRET_ACCESS_KEY']=config.get("KEYS", "AWS_SECRET_ACCESS_KEY")

LAST_UPDATE_DOWNLOAD_SITE=config.get("SITES", "LAST_UPDATE_DOWNLOAD_SITE")
COUNTRY_CODE_SITE=config.get("SITES", "COUNTRY_CODE_SITE")
CORONA_STATS_SITE=config.get("SITES", "CORONA_STATS_SITE")

JDBC_URL=config.get("DB", "JDBC_URL")
JDBC_URL_SCHEMA=config.get("DB", "JDBC_URL_SCHEMA")
JDBC_USER=config.get("DB", "JDBC_USER")
JDBC_PASSWORD= config.get("DB", "JDBC_PASSWORD")

EVENT_HEADER= config.get("CSVHEADER", "EVENT_HEADER")

# Fetch country data
source_event_url_content = requests.get(LAST_UPDATE_DOWNLOAD_SITE).content
source_event_url = str(source_event_url_content,"utf8").split("\n")[0].split(" ")[2]
source_country_name_url_content = str(requests.get(COUNTRY_CODE_SITE).content, "utf8")
# convert arrOfCountryCodeRaw into map of country_code -> country_name mapping.
dictOfCountryCode = dict(item.split("\t") for item in source_country_name_url_content[:-1].split("\n"))
dictOfCountryCodeInv = {v: k for k, v in dictOfCountryCode.items()}

checkIfExistsInDict = udf(lambda x: x in dictOfCountryCode.keys(), BooleanType())
countryCodeUdf= udf(lambda x: dictOfCountryCodeInv.get(x),StringType())
uuidUdf= udf(lambda : str(uuid.uuid4()),StringType())

def create_spark_session():
    """
     Creates the spark session
    """
    spark = SparkSession \
        .builder \
        .config("spark.jars.packages", "org.apache.hadoop:hadoop-aws:2.7.3,org.postgresql:postgresql:9.4.1207.jre7") \
        .getOrCreate()
    return spark
    
def process_country_dim_data(spark, eventDf, coronaFctDf):
    
    existingCountryDf = spark.read \
    .format("jdbc") \
    .option("url", JDBC_URL) \
    .option("dbtable",JDBC_URL_SCHEMA + "country_dim") \
    .option("user", JDBC_USER) \
    .option("password", JDBC_PASSWORD) \
    .option("driver", "org.postgresql.Driver")\
    .load()
    
    filteredDs = eventDf.filter(checkIfExistsInDict(eventDf.Actor1CountryCode) | checkIfExistsInDict(eventDf.Actor2CountryCode)).filter(eventDf.Sourceurl.like("%corona%"))

    countryDimDfUnionLeft = filteredDs.select(filteredDs.Actor1CountryCode.alias("country_code"));
    countryDimDfUnionRight = filteredDs.select(filteredDs.Actor2CountryCode.alias("country_code"));

    unionedDf = countryDimDfUnionLeft.union(countryDimDfUnionRight)

    countryDimDf = unionedDf.filter(checkIfExistsInDict(unionedDf.country_code)).withColumn("id", uuidUdf())

    countryDimDf = countryDimDf.join(coronaFctDf, "country_code").select("country_code", "country_name", "total_confirmed", "total_deaths", "total_recovered", "id")

    countryDimDf = countryDimDf.union(existingCountryDf);

    countryDimDf = countryDimDf.dropDuplicates(["country_code"])

    countryDimDf.write.mode("overwrite") \
        .format("jdbc") \
        .option("url", JDBC_URL) \
        .option("dbtable",JDBC_URL_SCHEMA + "country_dim") \
        .option("user", JDBC_USER) \
        .option("password", JDBC_PASSWORD) \
        .option("driver", "org.postgresql.Driver")\
        .save();
    
def process_corona_data(spark):
    
    coronaDf = spark.read.format("csv").option("delimiter", "\t").option("inferschema", "true").option("header", "true").load("data/corona.csv")
    modDf = coronaDf.groupBy("Country/Region").agg(F.sum("Confirmed").alias("total_confirmed"), F.sum("Deaths").alias("total_deaths"), F.sum("Recovered").alias("total_recovered"))

    modDf = modDf.withColumn("Country/Region", F.when(modDf["Country/Region"] == "Mainland China", "China").otherwise(modDf["Country/Region"]))
    modDf = modDf.withColumn("Country/Region", F.when(modDf["Country/Region"] == "US", "United States").otherwise(modDf["Country/Region"]))
    modDf = modDf.withColumn("Country/Region", F.when(modDf["Country/Region"] == "UK", "United Kingdom").otherwise(modDf["Country/Region"]))
    dictOfCountryCodeInv = {v: k for k, v in dictOfCountryCode.items()}

    

    modDf = modDf.withColumn("country_code", countryCodeUdf(modDf["Country/Region"]))

    coronaFctDf = modDf.select(F.col("Country/Region").alias("country_name"), "country_code", "total_confirmed", "total_deaths", "total_recovered")

    coronaFctDf.write.mode("overwrite") \
        .format("jdbc") \
        .option("url", JDBC_URL) \
        .option("dbtable",JDBC_URL_SCHEMA + "corona_facts") \
        .option("user", JDBC_USER) \
        .option("password", JDBC_PASSWORD) \
        .option("driver", "org.postgresql.Driver")\
        .save();
    return coronaFctDf

def process_event_data(spark):
    
   
    
    df = spark.read.format("csv").option("delimiter", "\t").option("inferschema", "true").option("header", "true").load("data/source_event.csv")

    filteredDs = df.filter(checkIfExistsInDict(df.Actor1CountryCode) | checkIfExistsInDict(df.Actor2CountryCode)).filter(df.Sourceurl.like("%corona%"))

    union1Df = filteredDs.select(filteredDs.Actor1CountryCode.alias("country_code"), filteredDs.Sourceurl.alias("url"), filteredDs.Dateadded.alias("date_added"))
    union2Df = filteredDs.select(filteredDs.Actor2CountryCode.alias("country_code"), filteredDs.Sourceurl.alias("url"), filteredDs.Dateadded.alias("date_added"))

    unionDf = union1Df.union(union2Df);

    unionDf = unionDf.filter("country_code IS NOT NULL").dropDuplicates(["country_code", "url"])

    newsEventsFactDf = unionDf.withColumn("id", uuidUdf())

    newsEventsFactDf.write.mode("append") \
        .format("jdbc") \
        .option("url", JDBC_URL) \
        .option("dbtable",JDBC_URL_SCHEMA + "news_events_fct") \
        .option("user", JDBC_USER) \
        .option("driver", "org.postgresql.Driver")\
        .option("password", JDBC_PASSWORD) \
        .save();
    
    return df;

    
def fetch_data():
    
    # Fetch event data
    urllib.request.urlretrieve(source_event_url, "data/event.csv.zip")
    subprocess.call("unzip data/event.csv.zip -d data && tail -n +1 data/*.CSV > data/temp.csv && sleep 1 &&" + 
                     "echo \"" + EVENT_HEADER + "\" > " + "data/header.txt && " + 
                     " cat data/header.txt data/temp.csv > data/source_event.csv && rm data/*.export.CSV", shell=True)

    # Fetch corona data
    source_corona_content = str(requests.get(CORONA_STATS_SITE).content, "utf8")
    subprocess.call("echo \"" + source_corona_content + "\"" + " > data/corona.csv", shell=True)


def main():
    """
    The main point of entry.
    """

    spark = create_spark_session()
    
    fetch_data()
    eventDf = process_event_data(spark)
    coronaDf = process_corona_data(spark)
    process_country_dim_data(spark, eventDf, coronaDf)


if __name__ == "__main__":
    main()
