# kotlin-pricealert
kotlin and spek unit test for price alert

alertlist(id, user, symbol, price, direction)

addPriceAlert: build tree map for symbol->price->alertlist

removePriceAlert: traverse tree map and search for symbol and user

checkPriceAlert: check level triggered and edge triggered price alert.

jersey RESTful Services
