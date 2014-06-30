Geohash Facet Plugin for elasticsearch
==========================================

Original project: https://github.com/zenobase/geocluster-facet

Installation (latest version): run

```
bin/plugin --url https://github.com/triforkams/geohash-facet/releases/download/geohash-facet-0.0.12/geohash-facet-0.0.12.jar --install geohash-facet
```


For usage see [this blog post](http://blog.trifork.com/2013/08/01/server-side-clustering-of-geo-points-on-a-map-using-elasticsearch/).

Versions
--------

<table>
    <thead>
		<tr>
			<th>geohash-facet</th>
			<th>elasticsearch compatibility</th>
			<th>notes</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>0.0.17</td>
			<td>1.2.1</td>
			<td>upgraded to ES 1.2.1</td>
		</tr>
		<tr>
			<td>0.0.16</td>
			<td>1.0.0</td>
			<td>fix for https://github.com/triforkams/geohash-facet/issues/9</td>
		</tr>
		<tr>
			<td>0.0.15</td>
			<td>1.0.0</td>
			<td>merged https://github.com/triforkams/geohash-facet/pull/6</td>
		</tr>
		<tr>
			<td>0.0.14</td>
			<td>1.0.0</td>
			<td>implemented https://github.com/triforkams/geohash-facet/issues/7</td>
		</tr>
		<tr>
			<td>0.0.13</td>
			<td>1.0.0</td>
			<td>bug fixing, added a facet builder for use on the client side</td>
		</tr>
		<tr>
			<td>0.0.12</td>
			<td>0.90.6+, 1.0.0+</td>
			<td>implemented https://github.com/triforkams/geohash-facet/issues/4</td>
		</tr>
		<tr>
			<td>0.0.11</td>
			<td>0.90.6+, 1.0.0+</td>
			<td>fixed https://github.com/triforkams/geohash-facet/issues/3</td>
		</tr>
		<tr>
			<td>0.0.10</td>
			<td>0.90.6+, 1.0.0+</td>
			<td>updated to stay compatible with latest ES</td>
		</tr>
		<tr>
			<td>0.0.9</td>
			<td>0.90.5</td>
			<td>updated to stay compatible with latest ES</td>
		</tr>
		<tr>
			<td>0.0.8</td>
			<td>0.90.3</td>
			<td>updated to stay compatible with latest ES</td>
		</tr>
		<tr>
			<td>0.0.7</td>
			<td>0.90.2</td>
			<td></td>
		</tr>
	</tbody>
</table>


Parameters
----------

<table>
	<tbody>
		<tr>
			<th>field</th>
			<td>The name of a field of type `geo_point`.</td>
		</tr>
		<tr>
            <th>factor</th>
        	<td>Controls the amount of clustering, from 0.0 (don't cluster any points) to 1.0 (create a single cluster containing all points).
        	Defaults to 0.1. The value determines the size of the cells used to cluster together points.
        	Starting from version 0.0.14, the clustering is computed using a bit-string geohash
        	instead of the traditional alphanumeric geohash. This gives you more fine grained selection
        	of the level of clustering.</td>
        </tr>
		<tr>
            <th>show_geohash_cell</th>
        	<td>Boolean. If true, for each cluster included in the reply the coordinates
        	of the corresponding geohash cell are provided (top left and bottom right corner.
        	Defaults to false.</td>
        </tr>
		<tr>
            <th>show_doc_id</th>
        	<td>Boolean. If true, for each cluster composed of a single document the document ID is returned.
        	Defaults to false.</td>
        </tr>

	</tbody>
</table>


Example
-------

Mapping: all you need is to declare the field containing the location as a type `geo_point`.

```javascript
{
  "venues" : {
    "properties" : {
      "location" : {
        "type" : "geo_point"
      }
    }
  }
}
```

Example document:

```javascript
{
    "took" : 42,
    "timed_out" : false,
    "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
    },
    "hits" : {
        "total" : 1,
        "max_score" : 1.0,
        "hits" : [ {
            "_index" : "myindex",
            "_type" : "venues",
            "_id" : "abc",
            "_score" : 1.0,
            "_source" : {
                "location":{ "lat":"52.01010835419531","lon":"4.722006599999986" }
            }
        }]
    }
}
```

Query:

```javascript
{
    "query" : { ... },
    "facets" : {
        "places" : {
            "geohash" : {
                "field" : "location",
                "factor" : 0.9
            }
        }
    }
}
```

Result:

```javascript
{
    "took" : 67,
    "timed_out" : false,
    "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
    },
    "hits" : {
        "total" : 1372947,
        "max_score" : 0.0,
        "hits" : [ ]
    },
    "facets" : {
        "places" : {
            "_type" : "geohash",
            "factor" : 0.9,
            "clusters" : [ {
                "total" : 8,
                "center" : {
                    "lat" : 16.95292075,
                    "lon" : 122.036081375
                },
                "top_left" : {
                    "lat" : 33.356026,
                    "lon" : 121.00589
                },
                "bottom_right" : {
                    "lat" : 14.60962,
                    "lon" : 129.247421
                }
            }, {
                "total" : 191793,
                "center" : {
                    "lat" : 52.02785559813162,
                    "lon" : 4.921446953767902
                },
                "top_left" : {
                    "lat" : 64.928595,
                    "lon" : 3.36244
                },
                "bottom_right" : {
                    "lat" : 45.468945,
                    "lon" : 26.067386
                }
            } ]
        }
    }
}
```

Query with show_geohash_cell enabled:

```javascript
{
    "query" : { ... },
    "facets" : {
        "places" : {
            "geohash" : {
                "field" : "location",
                "factor" : 0.9,
                "show_geohash_cell" : true
            }
        }
    }
}
```

Result:

```javascript
{
    "took" : 61,
    "timed_out" : false,
    "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
    },
    "hits" : {
        "total" : 1372947,
        "max_score" : 0.0,
        "hits" : [ ]
    },
    "facets" : {
        "places" : {
            "_type" : "geohash",
            "factor" : 0.9,
            "clusters" : [ {
                "total" : 8,
                "center" : {
                    "lat" : 16.95292075,
                    "lon" : 122.036081375
                },
                "top_left" : {
                    "lat" : 33.356026,
                    "lon" : 121.00589
                },
                "bottom_right" : {
                    "lat" : 14.60962,
                    "lon" : 129.247421
                },
                "geohash_cell" : {
                    "top_left" : {
                        "lat" : 45.0,
                        "lon" : 90.0
                    },
                    "bottom_right" : {
                        "lat" : 0.0,
                        "lon" : 135.0
                    }
                }
            }, {
                "total" : 191793,
                "center" : {
                    "lat" : 52.02785559813162,
                    "lon" : 4.921446953767902
                },
                "top_left" : {
                    "lat" : 64.928595,
                    "lon" : 3.36244
                },
                "bottom_right" : {
                    "lat" : 45.468945,
                    "lon" : 26.067386
                },
                "geohash_cell" : {
                    "top_left" : {
                        "lat" : 90.0,
                        "lon" : 0.0
                    },
                    "bottom_right" : {
                        "lat" : 45.0,
                        "lon" : 45.0
                    }
                }
            } ]
        }
    }
}
```


License
-------

```

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012-2013 Trifork Amsterdam BV

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
