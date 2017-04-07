DataVec Scripting Language
==========================

!!!Warning: this project is in a research state and must no be used for production purposes!!!

DataVecSL is a Proof of technical concept to wrap DL4J DataVec component into data manipulation DSL
---

While working on this project I quickly came to the realization that the problem I intended to solve is not a DataVec specific problem. It persists throughout the entire suit of tools available to the scientific community working in the domain of AI.

On one hand, the industry is rapidly coming up with new and better tools such as DL4J, Keras, Spark DataFrames, R, Dataframes.jl, etc. At least a dozen or more look them up in [Frameworks and Languages](https://github.com/vlovsky/DataVecSL/blob/master/Frameworks%20and%20Languages.md). The landscape is changing so rapidly, that we don't recognize it over the period of 1-3 years.  Andrew C. Oliver provided [his perspective](http://www.infoworld.com/article/3019754/application-development/16-things-you-should-know-about-hadoop-and-spark-right-now.html) related to this.

On the other hand, with so much diversity, the users are suck in a never ending cycle of relearning how to do actual stuff. But wait there is more. Vendors are trumping their own share of the market. If, for instance, there is a great engine for working with DataFrames that is implemented in a given language. That makes it available only to those using that language. 

Let's learn from the history. And let's take SQL as an example. I was too young to witness how the SQL was struggling to be born, as numerous RDBMS vendors and ANSI, and later ISO were trying to get on the same page as to what should be the scope of the SQL. There were quite a few SQL predecessors and alternatives. But no matter how badly people talk about SQL, it is a popular language. My quite long experience proves it to myself that SQL served its intended purpose well. And the purpose was to be a domain specific language to mange and use data according to the Relational model.

Today we need something slightly different although still related to data manipulation. The difference is the mathematical model - not only relational, and so it's not only SQL. And from where we are right now with the variety of the languages, it seems that we are bound to repeat the history. Providers of various tools will either fight over the market for their share of users. Eventually, there will be one o two high level domain specific languages that serve the needs of the users.

So we have great tools, passioned developers and users. What we are missing is the appropriate levels of data and computational abstractions. IT community goal should be to give the users frameworks that can be used interchangeably. Major decoupling should happen at the level of user-system interaction (such as language that is used by users to manage, view and manipulate data). Another level is between the major data management and computational components. Wes McKinney, the creator of Python Data Analysis Library - pandas, had discussed similar [thoughts](https://www.youtube.com/watch?v=stlxbC7uIzM&feature=youtu.be&t=220) at New York R Conference. Here are the [slides on slide share](https://www.slideshare.net/wesm/dataframes-the-good-bad-and-ugly) (slide 11 and 12). Decoupling is the key word there.

To generalize the initial idea of this project, it can be thought of as an attempt to develop an engine independent language for working with data with respect to deep learning. The intuition prompts that the language will not be a general purpose language, rather a DSL. Hopefully, it will be simple and concise enough to be implemented as libraries in different general purpose languages. I have a pretty good example of how this might be done. Let's recall a library called [jOOQ](https://en.wikipedia.org/wiki/Java_Object_Oriented_Querying). Here is an example of how it maps to SQL:

SQL Code:
```
 SELECT * FROM AUTHOR a
        WHERE EXISTS (SELECT 1
                   FROM BOOK
                  WHERE BOOK.STATUS = 'SOLD OUT'
                    AND BOOK.AUTHOR_ID = a.ID);
```

Equivalent in jOOQ:
``` 
 create.selectFrom(table("AUTHOR").as("a"))
        .where(exists(selectOne()
                     .from(table("BOOK"))
                     .where(field("BOOK.STATUS").equal(field("BOOK_STATUS.SOLD_OUT")))
                     .and(field("BOOK.AUTHOR_ID").equal(field("a.ID")))));
```

For now, I'll start from small and will attempt to gather somewhat comprehensive comparison of various aspects of data manipulation and analysis in different languages available to date.

## Build and Run

Use [Maven](https://maven.apache.org/) to build.

## References and notes
Oracle SQL\*Loader was used as an inspiration for many features of the DSL. However, syntax of the DataVec Scripting
Language is not fully compliant with the SQL*Loader. See [SQL\*Loader documentation](http://docs.oracle.com/database/121/SUTIL/toc.htm)
for more information on formal syntax.

DataVec is a data processing component in the [Deeplearning4J stack](https://github.com/deeplearning4j). See [DataVec user guide](https://deeplearning4j.org/etl-userguide) for more info.

## Documentation
DataVec Scripting Language was initially created to prove viability of a DSL as a facade in front of DataVec. It includes Parser and Executor. Parser intended to work with data load configuration scripts and creates AST. Executor subsequently maps the AST to the DataVec calls.

This component was built and tested with the following basic script. Source data is borrowed from the DataVec examples and included for convenience.

```
rem an example of load control file for loading basic DataVec example
load data
from :1
fields terminated by ','
into var
when TransactionAmountUSD >= 0 and MerchantCountryCode in enum("USA","CAN")
as (
    DateTimeString string filler,
    rem Following will trigger unsupported exception if uncommented
    rem HourOfDay integer extract(hour, toDate(DateTimeString, "YYYY-MM-DD HH:mm:ss.SSS", "UTC")),
    CustomerID string filler,
    MerchantID string filler,
    NumItemsInTransaction integer,
    MerchantCountryCode enum("USA","CAN","FR","MX"),
    TransactionAmountUSD double (if 50.0 > TransactionAmountUSD then 0.0 else TransactionAmountUSD),
    FraudLabel enum("Fraud","Legit")
);
```
