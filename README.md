DataVec Scripting Language
==========================

!!!Warning: project is in the research state and must no be used for production purposes.!!!
Proof of technical concept to wrap DL4J DataVec component into data manipulation DSL
---

## Build and Run

Use [Maven](https://maven.apache.org/) to build.

## References and notes
Oracle SQL*Loader was used as an inspiration for many features of the DSL. However, syntax of the DataVec Scripting
Language is not fully compliant with the SQL*Loader. See [SQL*Loader documentation](http://docs.oracle.com/database/121/SUTIL/toc.htm)
for more information on formal syntax.

DataVec is a data processing component in the Deeplearning4J stack. See [DataVec user guide](https://deeplearning4j.org/etl-userguide) for more info.

## Documentation
This project includes Parser and Executor. Parser intended to work on data load script to create AST. Executor
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
