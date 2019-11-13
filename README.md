# Transport Tycoon

Solution to [transport tycoon](https://github.com/Softwarepark/exercises/blob/master/transport-tycoon.md) kata.

## Solution 1
```$xslt
| :actor-id | :start-time | :end-time |   :action-type | :payload |
|-----------+-------------+-----------+----------------+----------|
|    :car-1 |           0 |         1 | :factory->port |       :a |
|    :car-1 |           1 |         2 | :port->factory |          |
|    :car-1 |           2 |         7 |    :factory->b |       :b |
|    :car-1 |           7 |        12 |    :b->factory |          |
|    :car-1 |          12 |        17 |    :factory->b |       :b |
|    :car-1 |          17 |        22 |    :b->factory |          |
|    :car-2 |           0 |         1 | :factory->port |       :a |
|    :car-2 |           1 |         2 | :port->factory |          |
|    :car-2 |           2 |         3 | :factory->port |       :a |
|    :car-2 |           3 |         4 | :port->factory |          |
|    :car-2 |           4 |         9 |    :factory->b |       :b |
|    :car-2 |           9 |        14 |    :b->factory |          |
|    :car-2 |          14 |        15 | :factory->port |       :a |
|    :car-2 |          15 |        16 | :port->factory |          |
|    :car-2 |          16 |        21 |    :factory->b |       :b |
|    :car-2 |          21 |        26 |    :b->factory |          |
|   :ship-1 |           1 |         5 |       :port->a |       :a |
|   :ship-1 |           5 |         9 |       :a->port |          |
|   :ship-1 |           9 |        13 |       :port->a |       :a |
|   :ship-1 |          13 |        17 |       :a->port |          |
|   :ship-1 |          17 |        21 |       :port->a |       :a |
|   :ship-1 |          21 |        25 |       :a->port |          |
|   :ship-1 |          25 |        29 |       :port->a |       :a |
|   :ship-1 |          29 |           |       :a->port |          |
29
-----------------------------
```
