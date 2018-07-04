# crypto-exchange-metadata

A machine readable [json document](exchange-metadata.json) containing exchange metadata for crypto currency trading. 

Supported data points include:

 * currency symbols
 * market symbols
 * minimum order sizes
 * order price precision
 * order quantity precision
 * trade fees
 * withdrawal fees
 * deposit fees

Supported exchanges include:

 * anxpro
 * binance
 * bitbay
 * bitfinex
 * bitmarket
 * bitstamp
 * bittrex
 * btcmarkets
 * cex
 * coinfloor
 * dsx
 * gdax
 * gemini
 * kraken
 * poloniex
 * therock
 * quadrigacx
 * quoinex
 * wex
 
## File format

Where possible efforts are made to keep things readable by humans.  Maps are kept in alphabetical order, percentages are expressed as percentages rather than decimals.

Fields starting with a double underscore (eg `__notes: "....`) are not machine readable and should be regarded as comments.

## Naming conventions

Always use the symbols that the exchanges use, including capitalization.

We arbitrarily used the terms _base currency_ and _counter currency_ to represent names of the symbols in a currency pair.  
So a BTC/USD market would (typically) have the _base currency_ of BTC and _counter currency_ of USD.  That means a bid order
would result in buying BTC for USD.

Some exchanges reverse these symbols in their market names.  Some swap the terms _base_ and _counter_, and some use different 
terms altogether.  There doesn't appear to be any consistency.

#### Exchange name

Self explanatory.  Lower case, taken from the exchange url.

#### Markets

The market symbols are specific to the exchanges, there is no common language for these.

For each market in an exchange the following data points are modelled:

 * _price-precision_: the number of digits that can come after the decimal point in the market price

 * _quantity-precision_: the number of digits that can come after the decimal point in the order quantity for a market

 * _minimum-order-size_: self explanitory... it expressed with a currency to avoid ambiguity

```json
    "markets": {
      "BTCCAD": {
        "quantity-precision": 8,
        "minimum-order-size": "BTC 0.01",
        "price-precision": 5
      },
```

Minimum order sizes are typically expressed in units of the base currency:
  
```json
      "BCC-BTC": {
        "minimum-order-size": "BCC 0.00035"        
      },
```
  
If the minimum order size is in units of the counter currency then it is represented in the same format:

```json
      "BCC-BTC": {
        "minimum-order-size": "BTC 0.00003"        
      },
```

If there is a different minimum order size based on the direction of the order, is is represented as such:

```json
      "BCC-BTC": {
        "minimum-order-size": [
          "BCC 0.00035",
          "BTC 0.00003"
        ]
      },
```

In this example the minimum bid size is 0.00035BCC and the minimum ask size is 0.00003BTC. _note_: this format will be clarified soon.

#### Fees

Trading fees, deposit fees and withdrawal fees are modelled.  

Where possible a note is added with a url that leads to the exchange documentation about the fees.  Also, if the fees are available via the API this is noted.

Absolute fees are represented as a number where there ambiguity in the currency (such as a withdrawal fee):

```json
  "anxpro": {
    "withdrawl-fees": {
      "BTC": 0.002,
```

Percentages are represented as such, eg:

```json
    "trading-fees": {
      "USDTUSD": {
        "taker": "0.2%",
        "maker": "0.2%"
      }
    }
```

If there is a sensible default fee it can be expressed as such:

```json
    "trading-fees": {
      "default": {
        "taker": "0.26%",
        "maker": "0.16%"
      },
      "USDTUSD": {
        "taker": "0.2%",
        "maker": "0.2%"
      }
    }
```

Where the exchange offers multiple options for a fee, these are expressed as a map:

```json
    "withdrawl-fees": {
        "PLN": {
          "Wire bank transfer": 4,
          "ATM withdrawal (100 - 1000 PLN)": 10,
          "ATM withdrawal (2000 PLN)": 20
        }
```

Where a fee has a fixed and percentage part, these are represented as a map, eg:

```json
    "withdrawl-fees": {
      "HKD": {
        "options": [
          {
            "Bank Wire": {
              "fixed": 250,
              "rate": "1.00%"
            }
          }
        ]
      },
```

## Whats not supported (yet)

_wildcards_: to represent all Ethereum markets it might be nice to use `ETH*`.

_maximum order sizes_

_Order types_: market / limit / hidden / stop loss etc.

_Fee discounts_: these are relative to the user's exchange account and so can be included. They can usually be got via the an API call, or calculated from the users activity.

_Symbol translations_: each exchange uses its own symbols for both currencies and markets.

_Lending_: only spot markets are supported.

_currency precision_: useful for withdrawals or showing what funds are needed to execute an order.

# Finally...

PRs welcome!