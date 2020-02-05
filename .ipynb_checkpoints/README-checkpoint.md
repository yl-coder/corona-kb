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




Describe the data sets you're using. Where did it come from? What type of information is included? 


## Directory

 - data/ : Contains sample data files
 - dl.cfg : Configuration for aws access keys
 - etl.py: etl main file. You can run main() method here to start the etl process
 - README.md: This file that you are reading.