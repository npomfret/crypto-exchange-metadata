# crypto-exchange-metadata

A machine readable [json document](exchange-metadata.json) containing metadata for crypto currency trading.

## File format

Where possible efforts are made to keep thing readable by humans.  For example, maps are kept in alphabetical order, percentages are expressed as percentages (not decimals).

Fields starting with a double underscore (eg `__notes: "....`) are not machine readable and should be regarded as comments.

#### Exchange name

Self explanatory.  Lower case, taken from the exchange url.

#### Markets

The market symbols are specific to the exchanges, there is no common language for these.  The symbols that the exchanges use a used here exclusively.

_price-precision_: the number of digits that can come after the decimal point in the market price
_quantity-precision_: the number of digits that can come after the decimal point in the order quantity for a market

```json
    "markets": {
      "BCHEUR": {
        "price-precision": 1,
        "quantity-precision": 3
      },
```

#### Fees

Trading fees, deposit fees and withdrawal fees are supported.  

Where possible a note is added with a url that leads to the exchange documentation about the fees.  Also, if the fees are available via the API this is noted.

Absolute fees are represented as a number if there is no ambiguity in the currency, eg:

```json
  "anxpro": {
    "withdrawl-fees": {
      ...
      "BTC": 0.002,
```

Percentages are represented as such, eg:

```json
    "trading-fees": {
      "default": {
        "taker": "0.2%",
        "maker": "0.1%"
      }
    }
```

Where the exchange offers multiple options for a fee, these are expressed as a map, eg:

```json
    "withdrawl-fees": {
        ...
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

_Minium and maximum order sizes_: can be expressed in the base or counter currency

_Order types_: market / limit / hidden / stop loss etc

_Tick size_: exchanges have very specific tick sizes 

_Fee discounts_: these are relative to the exchange account. They can usually be got via the API, or calculated programmatically.

_Symbol translations_: each exchange uses its own symbols for both currencies and markets. Mapping between these is not the goal of this project.

_Lending_: only spot markets are supported (for now)


PRs welcome!