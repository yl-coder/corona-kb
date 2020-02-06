## Setup
Download postgresql-42.2.9.jar and include as jar dependency for spark


## How to run the ETL process.
1) Setup your cred at dl.cfg
2) Run the following commands

```
import etl as e;
e.main();
```

### Project Scope and Gather Data

#### Scope 
This project aims to provide minimal knowledge base on the coronavirus. The knowledge base will consist of the number of confirmed, death, recovered cases and the related news event of each country.


#### Describe and Gather Data 

There will be three data sets in this project. 

GDELTv2
Provides news event around the world, I will retrieve only events related to coronavirus, by the keyword "corona"

Link: http://data.gdeltproject.org/gdeltv2/lastupdate.txt

CAMEO Country Code Mapping 
Provides mapping between CAMEO country code standard to the country name

Link: https://www.gdeltproject.org/data/lookups/CAMEO.country.txt

Corona Stats
Provides the number of confirmed, deaths and recovered cases of the coronavirus around the world.

Link:https://docs.google.com/spreadsheets/d/1wQVypefm946ch4XDp37uZ-wartW4V7ILdg-qYiDXUHM/export?format=tsv


#### Explore and Assess the Data

Explore the Data: Identify data quality issues, like missing values, duplicate data, etc and Cleaning Steps

##### GDELTv2 Dataset
Since we only want event that is related to corona virus and it's related country. There could be events that is not related to a country or coronavirus. The event country can be represented in Actor1CountryCode and c field. However, these fields could be empty or null.

Necessary steps are in-place to make sure only valid values are considered. 
1) Null and empty values are filtered off.
2) Only rows with valid country_code in Actor1CountryCode and Actor1CountryCode are filtered


##### CAMEO Country Code Mapping
This is a well defined dataset. No necessary data cleaning is needed.

##### Corona Stats
This dataset only provide country name, and some of the used country name is not according to the CAMEO standard.
The first step of data cleaning is to perform transforming of country name into CAMEO standard.

The next value we need is country_code, we get this data point by doing a reverse lookup of the CAMEO country code mapping dataset.


#### Data Model

##### Conceptual Data Model


The data model is based on star schema. It is a extension of star schema where each dimension is from multiple facts.

One of the use case is to retrieve the number of corona cases and news event for each country.

By having this model, we can retrieve all relevant data by doing a join between country_dim and news_events_fct table.

The number of corona cases data is denormalized to country_dim for easy retrival.

#### Mapping Out Data Pipelines

For this project, there's no complex dependency needed for the use of airflow. For the case of this project, we can make use of AWS Batch service to trigger etl.py script every 15 mins.


#### Data Quality Checks


##### Checked if file has been downloaded in filesystem

check_file_exists("data/corona.csv")
check_file_exists("data/source_event.csv")

##### Check if there are records generated on the 3 tables

check_has_records("news_events_fct")
check_has_records("country_dim")
check_has_records("corona_facts")

#### Data dictionary
    
##### country_dim
| field_name      | data_type | description                                           |
|-----------------|-----------|-------------------------------------------------------|
| country_name    | text      | The country name                                      |
| country_code    | text      | Unique country code                                   |
| total_confirmed | bigint    | Total confirmed cases of corona virus for the country |
| total_deaths    | bigint    | Total deaths cases of corona virus for the country    |
| total_recovered | bigint    | Total recovered cases of corona virus for the country |

##### news_events_fct
| field_name   | data_type | description                       |
|--------------|-----------|-----------------------------------|
| url          | text      | THe url of the article            |
| country_code | text      | Unique country code               |
| date_added   | bigint    | The date added in YYYYMMDD format |
| id           | text      | unique uuid                       |


#### Rationale for the choice of tools and technologies for the project.
Tools and techologies used:
##### Spark 
Spark was used instead of pandas because of it's ability to scale horizontally in the event the data size grows. Spark was also used to decouple the data processing logic from the DB. This is for more flexibility in changing the data processing logic. 

##### Python
Python is used because it's one of the most well used language. Documentation and tutorial are readily avaible online. This allows better developer productivity

#### Postgres
Postgres was used because of it's compability with redshift. It's using the same driver to connect. For cost and reasonable datasize reason, this project uses postgres instead of redshift. Redshift can be used in the event the dataset grows larger in future.


#### Propose how often the data should be updated and why.


#### Write a description of how you would approach the problem differently under the following scenarios:

##### The data was increased by 100x.
Spark is used to allow us to scale horizontally, we can use the EMR cluster to use more worker nodes to process the data.

We should also convert the csv into parquet and partition the data accordingly inorder to reduce data skew.

##### The data populates a dashboard that must be updated on a daily basis by 7am every day.

We can use Airflow to set a schedule to run the etl before 7am. For e.g. 5am. Assuming the job will be completed by 2 hrs.

##### The database needed to be accessed by 100+ people.

We can setup read replica to increase the read capacity of the database.

## Directory

 - data/ : Temp folder to store tmp files
 - dl.cfg : Configuration for cred
 - sql_queries.py: sql queries to create the tables.
 - etl.py: etl main file. You can run main() method here to start the etl process
 - README.md: This file that you are reading.