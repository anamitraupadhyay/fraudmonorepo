# Fraudmonorepo
It's different implementation to detect credit card frauds happening with active parameters as well as passive analysis.

## Setup:
1> In main.js setup your api keys for Google maps api and Opencage api for the conversion of data to work.

2> In cli : Cd fraudmonorepo

            docker compose up

## Tech Stacks:
Quarkus(WebServlets), Lit(Web component based library by Google for frotend), Flask, Google Maps Api, Opencage Api, Mariadb, Docker

## Architectural diagram and Data-flow diagram:
![block](https://github.com/user-attachments/assets/5ed94142-24de-422a-bbfa-4ce3feca7fc7)
![dfd](https://github.com/user-attachments/assets/33af6360-c390-4940-b9a8-d20c65bca2d6)

## Features implemented using the tech stacks:
1> Lit: Takes 9 json fields(gets geocoding and population from api calling Google maps & Opencage) and sends it back to the quarkus backend.
![FraudDetection0_page-0001](https://github.com/user-attachments/assets/5b15fc31-f0d8-4022-bcb7-be53ec3fe9ac)

2> Quarkus: Implemented in Webservlet and Undertow has db access logs when fraud through cases or model has predicted also logs the 9 fields regradless.
  It has implemnted distance mismatch case where user transacted from a location and within a very short time he has done from transaction from huge distance,
  whose speed seems impossible then it will log fraud below are some Screenshots of this working.
  
  1st Transaction:
  ![FraudDetection3_page-0001](https://github.com/user-attachments/assets/bbca7b4e-1d68-465d-abdc-99390877fa8b)
  2nd Transaction:
  ![FraudDetection4_page-0001](https://github.com/user-attachments/assets/5cb6ab0a-5c1a-4cfd-8bd8-501d6a76d0c1)

  Again it also has Merchant Analytics Dashboard doing rigorous historical analysis on db in various other datas such as:
  >Is it a high risk hour or not.
  >Merchant location.
  >Total Transactions.
  >Unique Cards in that location overall.
  >Fraud Transactions happened in total.
  >Fraud Rate out of total transactions.
  >Card Diversity out of total cards transactions.
  >Average Transaction amount.

  These datas provide critical insights to banks or users using the service apart from model's prediction.

3> Flask: Hosts a model which takes the 9 data and outputs prediction is_fraud column of its field.

## Example docker Stacktraces:
![DockerStackTraces](https://github.com/user-attachments/assets/8e0c57de-0641-4009-bda7-23569a6b6665)
