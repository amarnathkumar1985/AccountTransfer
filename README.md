# AccountTransfer

The end point urls to test the api and samples.

1. Create the two diffrent account

URL - localhost:18080/v1/accounts

{
    "accountId":"123",
    "balance": "8000.00"
}

{
    "accountId":"124",
    "balance": "3000.00"
}

2. Amount transfer

URL - localhost:18080/v1/accounts/123/124/3000

validate all scenario

URL - localhost:18080/v1/accounts/123/124/-3000 - for negative balance    

URL - localhost:18080/v1/accounts/123/125/3000 - account does not exist

URL - localhost:18080/v1/accounts/123/124/11000 - amount transfer more than account balance


3. get account
   
URL - localhost:18080/v1/accounts/123
  
