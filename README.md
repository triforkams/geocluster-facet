Geohash Facet Plugin for elasticsearch
==========================================

Original project: https://github.com/zenobase/geocluster-facet

Installation (latest version): run

```
bin/plugin --url https://github.com/triforkams/geohash-facet/releases/download/geohash-facet-0.0.17/geohash-facet-0.0.17.jar --install geohash-facet
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
            <th>precision_bits</th>
        	<td>Instead of `factor`, you can also provide the used geohash bits directly
            (otherwise the `precision_bits` gets internaly computed from the `factor` parameter).
            Starting from version 0.0.14, the maximal precision is 60 bits.
            If you supply `factor` and `precision_bits`, only `precision_bits` are used.</td>
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


Index configuration
-------------------

In the mapping, you need to declare the field containing the location as a type `geo_point`.

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

Querying (HTTP)
---------------

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
            "precision_bits" : 6,
            "factor" : 0.9,
            "clusters_total": 2,
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
                "precision_bits" : 6,
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
            "precision_bits" : 6,
            "factor" : 0.9,
            "clusters_total": 2,
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

Querying (Java)
---------------

You can also do facet requests using the `GeoFacetBuilder` class included in the library:
```java
public class Example {

    public static void main(String[] args) {
    
        GeoFacetBuilder facetBuilder = new GeoFacetBuilder("monuments")
                .field("location")
                .factor(0.9)
                .showGeohashCell(false)
                .showDocId(true);

        Client client = ... // instantiate
        
        SearchResponse response = client.prepareSearch("poi")
                .setSearchType(SearchType.COUNT)
                .addFacet(facetBuilder)
                .execute()
                .actionGet();

        GeohashFacet geohashFacet = (GeohashFacet) response.getFacets().facetsAsMap().get("monuments");
        
        for (Cluster cluster: geohashFacet.getEntries()) {
        
            // do something 	
        }
    }

}

```

Size of the cells
-----------------

The table below shows the size of the cells defined by various values of the `factor` parameter. These data can be useful if you want to find the factor value which returns at most _n_ clusters given a bounding box to search on.

<table>
    <thead>
		<tr>
			<th>Factor (rounded)</th>
            <th>Precision bits</th>
			<th>Latitude delta (degrees)</th>
			<th>Longitude delta (degrees)</th>
		</tr>
	</thead>
	<tbody>
        <tr><td>1</td><td>0</td><td>180</td><td>360</td></tr>
        <tr><td>0.98</td><td>1</td><td>180</td><td>180</td></tr>
        <tr><td>0.97</td><td>2</td><td>90</td><td>180</td></tr>
        <tr><td>0.95</td><td>3</td><td>90</td><td>90</td></tr>
        <tr><td>0.93</td><td>4</td><td>45</td><td>90</td></tr>
        <tr><td>0.92</td><td>5</td><td>45</td><td>45</td></tr>
        <tr><td>0.9</td><td>6</td><td>22.5</td><td>45</td></tr>
        <tr><td>0.88</td><td>7</td><td>22.5</td><td>22.5</td></tr>
        <tr><td>0.87</td><td>8</td><td>11.25</td><td>22.5</td></tr>
        <tr><td>0.85</td><td>9</td><td>11.25</td><td>11.25</td></tr>
        <tr><td>0.83</td><td>10</td><td>5.625</td><td>11.25</td></tr>
        <tr><td>0.82</td><td>11</td><td>5.625</td><td>5.625</td></tr>
        <tr><td>0.8</td><td>12</td><td>2.8125</td><td>5.625</td></tr>
        <tr><td>0.78</td><td>13</td><td>2.8125</td><td>2.8125</td></tr>
        <tr><td>0.77</td><td>14</td><td>1.40625</td><td>2.8125</td></tr>
        <tr><td>0.75</td><td>15</td><td>1.40625</td><td>1.40625</td></tr>
        <tr><td>0.73</td><td>16</td><td>0.703125</td><td>1.40625</td></tr>
        <tr><td>0.72</td><td>17</td><td>0.703125</td><td>0.703125</td></tr>
        <tr><td>0.7</td><td>18</td><td>0.3515625</td><td>0.703125</td></tr>
        <tr><td>0.68</td><td>19</td><td>0.3515625</td><td>0.3515625</td></tr>
        <tr><td>0.67</td><td>20</td><td>0.17578125</td><td>0.3515625</td></tr>
        <tr><td>0.65</td><td>21</td><td>0.17578125</td><td>0.17578125</td></tr>
        <tr><td>0.63</td><td>22</td><td>0.087890625</td><td>0.17578125</td></tr>
        <tr><td>0.62</td><td>23</td><td>0.087890625</td><td>0.087890625</td></tr>
        <tr><td>0.6</td><td>24</td><td>0.0439453125</td><td>0.087890625</td></tr>
        <tr><td>0.58</td><td>25</td><td>0.0439453125</td><td>0.0439453125</td></tr>
        <tr><td>0.57</td><td>26</td><td>0.02197265625</td><td>0.0439453125</td></tr>
        <tr><td>0.55</td><td>27</td><td>0.02197265625</td><td>0.02197265625</td></tr>
        <tr><td>0.53</td><td>28</td><td>0.010986328125</td><td>0.02197265625</td></tr>
        <tr><td>0.52</td><td>29</td><td>0.010986328125</td><td>0.010986328125</td></tr>
        <tr><td>0.5</td><td>30</td><td>0.0054931640625</td><td>0.010986328125</td></tr>
        <tr><td>0.48</td><td>31</td><td>0.0054931640625</td><td>0.0054931640625</td></tr>
        <tr><td>0.47</td><td>32</td><td>0.00274658203125</td><td>0.0054931640625</td></tr>
        <tr><td>0.45</td><td>33</td><td>0.00274658203125</td><td>0.00274658203125</td></tr>
        <tr><td>0.43</td><td>34</td><td>0.001373291015625</td><td>0.00274658203125</td></tr>
        <tr><td>0.42</td><td>35</td><td>0.001373291015625</td><td>0.001373291015625</td></tr>
        <tr><td>0.4</td><td>36</td><td>0.0006866455078125</td><td>0.001373291015625</td></tr>
        <tr><td>0.38</td><td>37</td><td>0.0006866455078125</td><td>0.0006866455078125</td></tr>
        <tr><td>0.37</td><td>38</td><td>0.0003433227539062</td><td>0.0006866455078125</td></tr>
        <tr><td>0.35</td><td>39</td><td>0.0003433227539062</td><td>0.0003433227539062</td></tr>
        <tr><td>0.33</td><td>40</td><td>0.0001716613769531</td><td>0.0003433227539062</td></tr>
        <tr><td>0.32</td><td>41</td><td>0.0001716613769531</td><td>0.0001716613769531</td></tr>
        <tr><td>0.3</td><td>42</td><td>0.0000858306884766</td><td>0.0001716613769531</td></tr>
        <tr><td>0.28</td><td>43</td><td>0.0000858306884766</td><td>0.0000858306884766</td></tr>
        <tr><td>0.27</td><td>44</td><td>0.0000429153442383</td><td>0.0000858306884766</td></tr>
        <tr><td>0.25</td><td>45</td><td>0.0000429153442383</td><td>0.0000429153442383</td></tr>
        <tr><td>0.23</td><td>46</td><td>0.0000214576721191</td><td>0.0000429153442383</td></tr>
        <tr><td>0.22</td><td>47</td><td>0.0000214576721191</td><td>0.0000214576721191</td></tr>
        <tr><td>0.2</td><td>48</td><td>0.0000107288360596</td><td>0.0000214576721191</td></tr>
        <tr><td>0.18</td><td>49</td><td>0.0000107288360596</td><td>0.0000107288360596</td></tr>
        <tr><td>0.17</td><td>50</td><td>0.0000053644180298</td><td>0.0000107288360596</td></tr>
        <tr><td>0.15</td><td>51</td><td>0.0000053644180298</td><td>0.0000053644180298</td></tr>
        <tr><td>0.13</td><td>52</td><td>0.0000026822090149</td><td>0.0000053644180298</td></tr>
        <tr><td>0.12</td><td>53</td><td>0.0000026822090149</td><td>0.0000026822090149</td></tr>
        <tr><td>0.1</td><td>54</td><td>0.0000013411045074</td><td>0.0000026822090149</td></tr>
        <tr><td>0.08</td><td>55</td><td>0.0000013411045074</td><td>0.0000013411045074</td></tr>
        <tr><td>0.07</td><td>56</td><td>0.0000006705522537</td><td>0.0000013411045074</td></tr>
        <tr><td>0.05</td><td>57</td><td>0.0000006705522537</td><td>0.0000006705522537</td></tr>
        <tr><td>0.03</td><td>58</td><td>0.0000003352761269</td><td>0.0000006705522537</td></tr>
        <tr><td>0.02</td><td>59</td><td>0.0000003352761269</td><td>0.0000003352761269</td></tr>
        <tr><td>0.0</td><td>60</td><td>0.0000001676380634</td><td>0.0000003352761269</td></tr>
        </tbody>
</table>

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
