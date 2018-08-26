const ccxt = require("ccxt");
const request = require("request");
const fs = require("fs");

const IGNORE = {
    "coinmarketcap": "not an exchange",
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
    const currenciesBySymbol = {};

    const fiatData = await doGetJson("https://gist.githubusercontent.com/Fluidbyte/2973986/raw/b0d1722b04b0a737aade2ce6e055263625a0b435/Common-Currency.json");
    for (let code in fiatData) {
        const item = fiatData[code];
        currenciesBySymbol[code] = {
            name: item.name,
            type: "fiat"
        }
    }

    const coinmarketcap = new ccxt.coinmarketcap({verbose: false});
    const currencies = await coinmarketcap.fetchCurrencies();

    Object.values(currencies).forEach((item) => {
        currenciesBySymbol[item.code] = {
            name: item.name,
            coinmarketcapId: item.id,
            type: "crypto"
        }
    });

    return currenciesBySymbol;
}

async function go() {
    const currenciesBySymbol = await currencyMapping();

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

            const baseCurrency = currenciesBySymbol[base] ? currenciesBySymbol[base].name : base;
            const quoteCurrency = currenciesBySymbol[quote] ? currenciesBySymbol[quote].name : quote;

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
