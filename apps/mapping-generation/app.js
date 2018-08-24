const ccxt = require("ccxt");
const request = require("request");
const fs = require("fs");

const IGNORE = {
    "bitfinex": "duplicated",
    "bitstamp1": "duplicated",
    "coinbase": "duplicated (use coinbasepro)",
    "gdax": "duplicated (use coinbasepro)",
    "coinmarketcap": "not an exchange",
    "hitbtc": "duplicated",
};
const exchangeNames = ccxt.exchanges.filter((exchangeName) => !IGNORE.hasOwnProperty(exchangeName));
// const exchangeNames = ['bitfinex'];

function doGetJson(url) {
    return new Promise((resolve, reject) => {
        request(url, (err, res, body) => {
            if(err) {
                reject(err);
            } else {
                resolve(JSON.parse(body));
            }
        });
    });
}

async function currencyMapping() {
    const coinmarketcap = new ccxt['coinmarketcap'];
    const currencies = await coinmarketcap.fetchCurrencies();
    const currencyNamesById = {};

    const fiatData = await doGetJson("https://gist.githubusercontent.com/Fluidbyte/2973986/raw/b0d1722b04b0a737aade2ce6e055263625a0b435/Common-Currency.json");
    for (let code in fiatData) {
        const item = fiatData[code];
        currencyNamesById[code] = {
            name: item.name,
            type: "fiat"
        }
    }

    Object.values(currencies).forEach((item) => currencyNamesById[item.id] = {
        name: item.name,
        type: "crypto"
    });

    return currencyNamesById;
}

async function go() {
    const currencyNamesById = await currencyMapping();

    return Promise.all(exchangeNames.map(async (exchangeName) => {

        const exchange = new ccxt[exchangeName];
        try {
            await exchange.loadMarkets();
        } catch (e) {
            console.error(`failed to load markets for ${exchangeName}`, e);
            return;
        }

        const markets = exchange.markets;
        const output = [];

        Object.values(markets).forEach((market) => {
            const {id, symbol, base, quote, baseId, quoteId} = market;
            const baseCurrency = currencyNamesById[base] ? currencyNamesById[base].name : base;
            const quoteCurrency = currencyNamesById[quote] ? currencyNamesById[quote].name : quote;
            const ccxt = {symbol, base, quote};

            output.push({id, ccxt, baseId, quoteId, baseCurrency, quoteCurrency})
        });

        output.sort((a, b) => a.id.localeCompare(b.id));

        fs.writeFileSync(`data/${exchange.id}.json`, JSON.stringify(output, null, 2), 'utf8');
    }));
}

go().then(() => {
    console.log("done")
})
