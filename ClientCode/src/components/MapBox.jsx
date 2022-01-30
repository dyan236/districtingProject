import React, { useRef, useEffect, useState } from 'react';
import ReactDOM from 'react-dom';
import mapboxgl from 'mapbox-gl/dist/mapbox-gl-csp';
// eslint-disable-next-line import/no-webpack-loader-syntax
import MapboxWorker from 'worker-loader!mapbox-gl/dist/mapbox-gl-csp-worker';
import '../styles/MapBox.css'
import Sidebar from './Sidebar.jsx'
import MapFilter from './MapFilter.jsx'
 
mapboxgl.workerClass = MapboxWorker;
mapboxgl.accessToken = '';

function MapBox(){
    //State variables & methods
    const mapContainer = useRef();
    const [us_state, setUSState] = useState('');
    const [mapFiltering, setMapFiltering] = useState(false);
    const [currentDistricting, setCurrentDistricting] = useState(null);
    const [filterOptions, setFilterOptions] = useState([[], -1, -1, -1, -1, -1, -1]);
    const [objectiveOptions, setObjectiveOptions] = useState([-1, -1, -1, -1, -1, -1]);
    const [loaded, setLoaded] = useState(false);
    const [listIncumbents, setListIncumbents] = useState([]);
    const [reloadData, setReloadData] = useState(true);
    const [jobSummaries, setJobSummaries] = useState(null);
    const saveFilterOptions = (arr) =>{
        setFilterOptions(arr);
    }
    const getFilterOptions = () =>{
        return filterOptions;
    }
    const saveObjectiveOptions = (arr) =>{
        setObjectiveOptions(arr);
    }
    const changeCurrentDistricting = (districting) =>{
        setCurrentDistricting(districting);
    }
    const getCurrentDistricting = () =>{
        return currentDistricting;
    }
    const getJobSummaries = () =>{
        return jobSummaries;
    }

    //MapBox method
    //mapbox://styles/dayyan/cklwxkyku48q317rtghahqvmb
    //mapbox://styles/mapbox/dark-v10
    var map=useRef();
    useEffect(() => {
        map.current = new mapboxgl.Map({
            container: mapContainer.current,
            style: 'mapbox://styles/mapbox/dark-v10',
            center: [-70.9, 42.35],
            zoom: 4,
            pitchWithRotate: false,
            dragRotate: false,
            minZoom: 4,
            maxZoom: 12,
            maxBounds: [[-124.7844079, 24.7433195], [-66.9513812, 49.3457868]]
        });
        map.current.on('load', ()=>{
            console.log(map.current.getStyle());
            map.current.getStyle().layers.map((layer)=>map.current.setLayoutProperty(layer.id, 'visibility', 'visible'));
            setLoaded(true);
        })
        return () => map.current.remove();
    }, []);
    
    //Handler for when user selects a state
    const selectUSState = (state) => {
        //Still loading => stall
        if(!loaded) return state;
        var popup = new mapboxgl.Popup({
            closeButton: false,
            closeOnClick: false
        });
        const srcId='currentlyEnacted';
        const srcId2='precinctLines';
        setUSState(state);
        //Look-up state lon/lat for mapbox
        const data=require('../data/states.json')
        var stateObj=null;
        for(var i=0;i<data.length;i++){
            if(data[i].state===state)
                stateObj=data[i];
        }
        if(stateObj!=null){
            //Center on state, add sources from server
            map.current.flyTo({center: [stateObj.lon, stateObj.lat], zoom: stateObj.zoom});
            map.current.addSource(srcId, {
                type: 'geojson',
                data: 'http://localhost:8080/enacted?statename='+stateObj.state,
                promoteId: 'Code'
            });
            map.current.addSource(srcId2, {
                type: 'geojson',
                data: 'http://localhost:8080/precincts?statename='+stateObj.state,
                promoteId: 'ID'
            });
        }
        //Add layers to mapbox
        map.current.addLayer({
            'id': srcId2,   //precinct layer
            'type': 'fill',
            'source': srcId2,
            'layout': {'visibility': 'visible'},
            'paint':{
                'fill-color': '#088',
                'fill-opacity': [
                    'case',
                    ['boolean', ['feature-state', 'hover'], false],
                    0.5,
                    0.25
                    ],
                'fill-outline-color': 'black'
            }
        });
        map.current.addLayer({
            'id': srcId,    //enacted layer
            'type': 'fill',
            'source': srcId,
            'layout': {'visibility': 'visible'},
            'paint':{
                'fill-color': '#90ee90',
                'fill-opacity': [
                    'case',
                    ['boolean', ['feature-state', 'hover'], false],
                    0.5,
                    0.25
                    ],
                'fill-outline-color': 'white'
            }
        });
        //Add map interactivity
        var hoveredPrecinctId=null;
        map.current.on('mousemove', srcId2, function (e) {   //precinct layer first
            map.current.getCanvas().style.cursor = 'pointer';
            var coordinates = e.lngLat;
            const nf = new Intl.NumberFormat();
            var description = "<b>Precinct: "+e.features[0].properties.ID+"</b><br/> Total Population: "+nf.format(e.features[0].properties.TOTPOP)+"<br/> Hispanic: "
            +nf.format(e.features[0].properties.HISP)+"<br/> White: "+nf.format(e.features[0].properties.WHITE)+"<br/> Black: "+nf.format(e.features[0].properties.BLACK)
            +"<br/> American Indian/Alaska Native: "+nf.format(e.features[0].properties.AMIN)+"<br/> Asian: "+nf.format(e.features[0].properties.ASIAN)
            +"<br/> Native Hawaiian/Pacific Islander: "+nf.format(e.features[0].properties.NHPI)+"<br/> Other: "+nf.format(e.features[0].properties.OTHER)
            +"<br/> Voting Age Population: "+nf.format(e.features[0].properties.VAP);
            popup.setLngLat(coordinates).setHTML(description).addTo(map.current);
            if (e.features.length > 0) {
                if (hoveredPrecinctId) {
                    map.current.setFeatureState(
                        { source: srcId2, id: hoveredPrecinctId },
                        { hover: false }
                    );
                }
                hoveredPrecinctId = e.features[0].properties.ID;
                map.current.setFeatureState(
                    { source: srcId2, id: hoveredPrecinctId },
                    { hover: true }
                );
            }
        }); 
        map.current.on('mouseleave', srcId2, function () {
            map.current.getCanvas().style.cursor = '';
            popup.remove();
            if (hoveredPrecinctId) {
                map.current.setFeatureState(
                    { source: srcId2, id: hoveredPrecinctId },
                    { hover: false }
                );
            }
            hoveredPrecinctId = null;
        });
        //enacted layer second
        var hoveredStateId=null;
        map.current.on('mousemove', srcId, function (e) {   
            map.current.getCanvas().style.cursor = 'pointer';
            var coordinates = e.lngLat;
            const nf = new Intl.NumberFormat();
            var description = "<b>District: "+e.features[0].properties.District+"</b><br/> Incumbent: "+e.features[0].properties.incumbents
            +"<br/> Total Population: "+nf.format(e.features[0].properties.TOTPOP)+"<br/> Hispanic: "+nf.format(e.features[0].properties.HISP)
            +"<br/> White: "+nf.format(e.features[0].properties.WHITE)+"<br/> Black: "+nf.format(e.features[0].properties.BLACK)
            +"<br/> American Indian/Alaska Native: "+nf.format(e.features[0].properties.AMIN)+"<br/> Asian: "+nf.format(e.features[0].properties.ASIAN)
            +"<br/> Native Hawaiian/Pacific Islander: "+nf.format(e.features[0].properties.NHPI)+"<br/> Other: "+nf.format(e.features[0].properties.OTHER)
            +"<br/> Area: "+e.features[0].properties.area;
            popup.setLngLat(coordinates).setHTML(description).addTo(map.current);
            if (e.features.length > 0) {
                if (hoveredStateId) {
                    map.current.setFeatureState(
                        { source: srcId, id: hoveredStateId },
                        { hover: false }
                    );
                }
                hoveredStateId = e.features[0].properties.Code;
                map.current.setFeatureState(
                    { source: srcId, id: hoveredStateId },
                    { hover: true }
                );
            }
        }); 
        map.current.on('mouseleave', srcId, function () {
            map.current.getCanvas().style.cursor = '';
            popup.remove();
            if (hoveredStateId) {
                map.current.setFeatureState(
                    { source: srcId, id: hoveredStateId },
                    { hover: false }
                );
            }
            hoveredStateId = null;
        });
        //Retrieve job summaries from server
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/state?statename="+state;
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE: "+json);
                setJobSummaries(json);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
    }

    return (
        <div>
            <div className="map-container" ref={mapContainer} />
            <div className="mapFilterContainer">
                <button className="mapFilterButton" onClick={()=>setMapFiltering(!mapFiltering)}>Map Filters</button>
                { mapFiltering ? <MapFilter map={map}></MapFilter> : null}
            </div>
            {   us_state!=="" ? <Sidebar selectedUSState={us_state} saveFilterFunction={saveFilterOptions} savedFilterOptions={filterOptions} 
            saveObjectiveFunction={saveObjectiveOptions} savedObjectiveOptions={objectiveOptions} changeCurrentFunction={changeCurrentDistricting} 
            getCurrentFunction={getCurrentDistricting} map={map} setMapFiltering={setMapFiltering} listIncumbents={listIncumbents} setListIncumbents={setListIncumbents} 
            setUSState={setUSState} reloadData={reloadData} setReloadData={setReloadData} getJobSummaries={getJobSummaries} getFilterOptions={getFilterOptions} mapboxgl={mapboxgl}/> : 
                    <div>
                        <div className="entryBackground"></div>
                        <div className="entryWindow">
                            <p className="entryTitle">Select a state.</p>
                            <div className="entryDiv">
                                <select defaultValue="default" className="stateSelect">
                                    <option value="default" disabled>Select state... </option>
                                    <option value="Georgia">Georgia</option>
                                </select><br/><br/><br/>
                                <button className="stateSelectButton" onClick={()=>selectUSState("Georgia")}>Ok</button>
                            </div>
                        </div>
                    </div>
            }
        </div>
        );
}
ReactDOM.render(<MapBox />, document.getElementById('root'));
 
export default MapBox;