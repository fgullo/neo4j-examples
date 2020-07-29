//'transport-nodes.csv' must be in '<neo4j-home>\import directory'

WITH "file:///" AS base WITH base + "transport-nodes.csv" AS uri
LOAD CSV WITH HEADERS FROM uri AS row
MERGE (place:Place {id:row.id})
SET place.latitude = toFloat(row.latitude), place.longitude = toFloat(row.latitude), place.population = toInteger(row.population)