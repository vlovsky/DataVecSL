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