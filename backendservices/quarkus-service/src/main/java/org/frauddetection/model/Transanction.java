package org.frauddetection.model;

import java.
//3 classes format pojo for data logic travel and json data nesting for problem solving
//Transacion data is the final form of the json we will send to the frontend
class Transaction{
    int prediction;
    String Reason;
    String Warning;
    public Analytics analyticsobj;
}
class Analytics{
    boolean highRiskHour;
    public MerchantRisk merchriskobj;
}
class MerchantRisk{

}

//premade class for above class processing as it needs to be extracted from Transaction data since we dont need all hte 9 fields
//on second thought i may not need this as it can be obtained from sql and...no no actually i need to store the data in class format because in here i can store all these datas and the MerchantRisk is for the json response so no data storing for the cases am gonna write
class merchdataneeds{

}