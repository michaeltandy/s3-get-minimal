# Minimal S3 get request signing

Are you writing a library that needs to grab something from S3
but you don't want to introduce a potential dependency conflict 
by using a big library 
when you just want to read one file?

This project contains 
one file of code 
and one file of unit tests.
You can copy and paste one or both.

It takes care of 
calculating the HMAC-SHA1 auth header
and the date header,
and it can issue the GET request 
and read the response into a string
if you like.

It should also support the [S3 migration features of Google Cloud Storage](https://developers.google.com/storage/docs/migrating)

For more about S3 authentication, see [Amazon's documentation](http://docs.aws.amazon.com/AmazonS3/latest/dev/RESTAuthentication.html).

## How do I use it?

Check out the unit test file for usage examples.

## The unit tests are failing for me?

There are unit tests that run against real S3 and read real objects with real
credentials. I haven't given you the credentials for my account though.

The unit test looks in ~/aws.properties
if you want you can change the test to look elsewhere.

## What license is it under?

This project is (c) Michael Tandy
it's released under the [MIT license](http://en.wikipedia.org/wiki/MIT_License).
Feel free to copy and paste and modify.