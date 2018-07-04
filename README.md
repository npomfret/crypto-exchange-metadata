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
 * coinbase
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

Where possible efforts are made to keep thing readable by humans.  For example, maps are kept in alphabetical order, percentages are expressed as percentages (not decimals).

Fields starting with a double underscore (eg `__notes: "....`) are not machine readable and should be regarded as comments.

## Naming conventions

Always use the symbols that the exchanges use, including capitalization.

We arbitrarily used the terms _base currency_ and _counter currency_ to represent names of the symbols in a currency pair.  
So a BTC/USD market would (typically) have the _base currency_ of BTC and _counter currency_ of USD.  That means a bid order
would result in buying BTC for USD.

Some exchanges reverse these symbols in their market names.  Some swap the terms base and counter, and some use different 
terms altogether.

#### Exchange name

Self explanatory.  Lower case, taken from the exchange url.

#### Markets

The market symbols are specific to the exchanges, there is no common language for these.  The symbols that the exchanges use a used here exclusively.

_price-precision_: the number of digits that can come after the decimal point in the market price
_quantity-precision_: the number of digits that can come after the decimal point in the order quantity for a market
_minimum-order-size_: expressed with a currency to avoid ambiguity

```json
    "markets": {
      "ATENCEUR": {
        "quantity-precision": 0,
        "minimum-order-size": "ATENC 0.01",
        "price-precision": 8
      },
```

Minimum order sizes are typically measured in the base currency, eg:
  
```json
      "BCC-BTC": {

        "minimum-order-size": "BCC 0.00035"        
      },
```
  
If the minimum order size is in units of the counter currency, it is represented in the same format, eg:

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

In this example the minimum bid size is 0.00035BCC and the minimum ask size is 0.00003BTC. _note_ this will be changed shortly.

#### Fees

Trading fees, deposit fees and withdrawal fees are supported.  

Where possible a note is added with a url that leads to the exchange documentation about the fees.  Also, if the fees are available via the API this is noted.

Absolute fees are represented as a number if there is no ambiguity in the currency, eg:

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

If there is a sensible default it can be expressed as such:

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

Where the exchange offers multiple options for a fee, these are expressed as a map, eg:

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

_maximum order sizes_: can be expressed in the base or counter currency.

_Order types_: market / limit / hidden / stop loss etc.

_Fee discounts_: these are relative to the user's exchange account. They can usually be got via the an API call, or calculated programmatically.

_Symbol translations_: each exchange uses its own symbols for both currencies and markets.

_Lending_: only spot markets are supported (for now).

_currency precision_: useful for withdrawals or showing what funds are needed to execute an order.

# Finally...

PRs welcome!