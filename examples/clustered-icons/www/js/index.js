
var map;
var markers = [];

function clearMarkers() {
    while(markers.length){
        markers.pop().setMap(null);
    }
}

function addMarker(lat, lon, title, icon) {
    markers.push(new google.maps.Marker({
        position: new google.maps.LatLng(lat, lon),
        map: map,
        title: title,
        icon: icon,
        shadow: null
    }));
}

/**
 *  Adjust this to tune the clustering behavior
 */
var zoom2digits = {
    21: 12,
    20: 9,
    19: 8,
    18: 8,
    17: 7,
    16: 7,
    15: 7,
    14: 6,
    13: 6,
    12: 6,
    11: 5,
    10: 5,
    9: 4,
    8: 4,
    7: 3,
    6: 3,
    5: 3,
    4: 2,
    3: 2,
    2: 2,
    1: 1,
    0: 1
};

function fetchFacets() {
    var ne = map.getBounds().getNorthEast();
    var sw = map.getBounds().getSouthWest();
    var f = 1 - zoom2digits[map.zoom] / 12;
    console.log("querying with factor " + f);
    $.ajax({

        url: "http://" + window.location.hostname + ":9200/idx/objects/_search?search_type=count",
        contentType: "text/json",
        type: "POST",
        data: JSON.stringify({
            query: {
                filtered: {
                    query: {
                        match_all: {}
                    },
                    filter: {
                        geo_bounding_box: {
                            location: {
                                top_left: {
                                    "lat": ne.lat(),
                                    "lon": sw.lng()
                                },
                                bottom_right: {
                                    "lat": sw.lat(),
                                    "lon": ne.lng()
                                }
                            }
                        }
                    }
                }
            },
            facets: {
                places: {
                    geohash: {
                        field: "location",
                        factor: f
                    }
                }
            }
        }),
        dataType: "json"}
    )
    .done(function(data){
        clearMarkers();
        var clusters = data.facets.places.clusters;
         console.log('received ' + clusters.length + ' clusters');
        for (var i = 0; i < clusters.length; i++) {

                addMarker(
                        clusters[i].center.lat,
                        clusters[i].center.lon,
                        clusters[i].total == 1?
                            "single item @" + clusters[i].center.lat + ", " + clusters[i].center.lon:
                            "cluster (" + clusters[i].total + ") @" + clusters[i].center.lat + ", " + clusters[i].center.lon,
                        groupIcon(clusters[i].total)
                );
        }
    });
}

function groupIcon(groupSize) {
    return groupSize > 1?
        'https://chart.googleapis.com/chart?chst=d_map_spin&chld=1.0|0|FF8429|16|b|' + groupSize:
        'https://chart.googleapis.com/chart?chst=d_map_spin&chld=0.5|0|FF8429|16|b|';
}


function initialize(divId){

    initMap(divId);

}

function initMap(divId){
    var mapOptions = {
        zoom: 8,
        center: new google.maps.LatLng(52.37267, 4.89295),
        mapTypeId: google.maps.MapTypeId.ROADMAP
    };

    map = new google.maps.Map(document.getElementById(divId), mapOptions);

    google.maps.event.addDomListener(window, 'resize', function(){ fetchFacets(); } );
    google.maps.event.addListener(map, 'dragend', function(){ fetchFacets(); } );
    google.maps.event.addListener(map, 'zoom_changed', function(){ fetchFacets(); } );

    fetchFacets();
}

