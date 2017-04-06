DataVec Scripting Language
==========================

!!!Warning: this project is in the research state and must no be used for production purposes!!!

DataVecSL is a Proof of technical concept to wrap DL4J DataVec component into data manipulation DSL
---

While working on this project I quickly came to realization that the problem I intended to solve is not a DataVen specific problem. It presists throughout the entrire suit of tools available to the scientists working in this domain. On one hand, the industry is rapidly coming up with new and better tools such as DL4J, Keras, Spark DataFrames, R, Dataframes.jl, etc. At least a dozen or more. On the other, with so much deversity, the users are suck in a never ending loop of relearning how to do actual stuff. To generalize the initial idea of this project, it can be thought of as an attempt to develop an engine independent language for working with data. I realize this is a noble goal that one person isn't going to achieve and that's why this project is in reasearch phase.

To backup my words up, here are [few related but independant thoughts](https://www.youtube.com/watch?v=stlxbC7uIzM&feature=youtu.be&t=220) from ... creator of Python Data Analysis Library - pandas. Here are the [slides on slide share](https://www.slideshare.net/wesm/dataframes-the-good-bad-and-ugly) (slide 11 and 12). Decoupling is the key word.

## Build and Run

Use [Maven](https://maven.apache.org/) to build.

## References and notes
Oracle SQL\*Loader was used as an inspiration for many features of the DSL. However, syntax of the DataVec Scripting
Language is not fully compliant with the SQL*Loader. See [SQL\*Loader documentation](http://docs.oracle.com/database/121/SUTIL/toc.htm)
for more information on formal syntax.

DataVec is a data processing component in the [Deeplearning4J stack](https://github.com/deeplearning4j). See [DataVec user guide](https://deeplearning4j.org/etl-userguide) for more info.

## Documentation
This project includes Parser and Executor. Parser intended to work with data load script and creates AST. Executor
subsequently maps the AST to the DataVec calls.

This component was built and tested with the following basic script. Source data is borrowed from the DataVec examples and
included for convenience.

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
