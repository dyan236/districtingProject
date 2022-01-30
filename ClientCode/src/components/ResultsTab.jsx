import React, { useState } from 'react';
import '../styles/ResultsTab.css'
import loading from '../resources/loading.gif'

function ResultsTab(props){
    //State variables & methods
    const [sortCategory, setSortCategory] = useState("objScore");
    const sortCategories = ["Top 10", "High Score(Enacted)", "Desired Maj-Min", "Area Pair-Deviation"];
    const getJSONSummary = () => {
        return props.getJSONSummary();
    }
    const getJSONListing = () => {
        return props.getJSONListing();
    }
    const setJSONListing = (arr) =>{
        props.setJSONListing(arr);
    }
    const getFilterOptions = () =>{
        return props.getFilterOptions();
    }

    //Fills top ten districtings
    const populateList = () =>{
        if(getJSONListing()!=null){
            applySort();
            return getJSONListing().slice(0,10).map((districting)=>{
                if(sortCategory!=="majMin" || (districting.majMinDistricts==getFilterOptions()[3])){
                    return <div style={{paddingBottom: '24px'}}><table className="resultsTable">
                        <tr>
                            <td style={{color: 'white', backgroundColor: 'darkslategray', width: '187px'}}><b>Districting[{districting.index}]</b></td>
                            <td style={{width: '187px'}}>Obj. Score: {districting.objScore.toFixed(2)}</td>
                            <td style={{width: '187px'}}>Pop. Equality: {districting.popScore.toFixed(2)}</td>
                            <td style={{width: '187px'}}>Split Counties: --</td>
                        </tr>
                        <tr>
                            <td>Deviation (Avg): {districting.deviationAverage.toFixed(2)}</td>
                            <td>Deviation (Enac.): {districting.deviationEnacted.toFixed(2)}</td>
                            <td>Compactness: {districting.compactnessScore.toFixed(2)}</td>
                            <td>Pol. Fairness: --</td>
                        </tr>
                        <tr>
                            <td colspan="4" style={{padding: '1px'}}><button onClick={()=>{hideEnacted(); props.changeCurrentFunction(districting); requestDistricting(districting.index);}}>View</button></td>
                        </tr>
                    </table></div>
                }
                else return null;
            });
        }
    }

    //Fills sorting options
    const populateOptions = ()=>{
        //data.sort(distCompare);
        return sortCategories.map((option) =>{
            return <option value={option}>{option}</option>
        });
    }

    //TODO
    const handleSortCatChange = event =>{
        switch(event.target.value){
            case 'Area Pair-Deviation':
                setSortCategory('areaPair');
                break;
            case 'High Score(Enacted)':
                setSortCategory('highScore');
                break;
            case 'Desired Maj-Min':
                setSortCategory('majMin');
                break;
            default:
                setSortCategory('objScore');
                break;
        }
    }

    const sortFunc = (a, b) =>{
        switch(sortCategory){
            case "areaPair":
                return a.deviationEnacted-b.deviationEnacted;
            case "highScore":
                return b.deviationEnacted-a.deviationEnacted;
            default:
                return b.objScore-a.objScore;
        }
    }

    const applySort = () =>{
        var tmp=getJSONListing();
        tmp.sort(sortFunc);
        setJSONListing(tmp);
    }

    const hideEnacted = () =>{
        props.map.current.setLayoutProperty('currentlyEnacted', 'visibility', 'none');
        props.setMapFiltering(false);
    }

    const requestDistricting = (index) =>{
        if (props.map.current.getLayer('currentlyViewed')) 
            props.map.current.setLayoutProperty('currentlyViewed', 'visibility', 'none');
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/view?districtingId="+index;
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE: "+this.responseText);
                showDistricting(json);
            }
            else if(this.readyState===4 && this.status===400){
                alert("Please apply weights first. ");
                return;
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
    }

    const showDistricting = (geojson) =>{
        if (props.map.current.getLayer('currentlyViewed')) {
            props.map.current.removeLayer('currentlyViewed');
        }
        if (props.map.current.getSource('currentlyViewed')) {
            props.map.current.removeSource('currentlyViewed');
        }
        props.map.current.addSource('currentlyViewed', {
            type: 'geojson',
            data: geojson,
            promoteId: 'districtNumber'
        });
        props.map.current.addLayer({
            'id': 'currentlyViewed',
            'type': 'fill',
            'source': 'currentlyViewed',
            'layout': {'visibility': 'visible'},
            'paint':{
                'fill-color': '#ffffed',
                'fill-opacity': [
                    'case',
                    ['boolean', ['feature-state', 'hover'], false],
                    0.5,
                    0.25
                    ],
                'fill-outline-color': 'white'
            }
        });
        var hoveredStateId=null;
        var popup = new props.mapboxgl.Popup({
            closeButton: false,
            closeOnClick: false
        });
        props.map.current.on('mousemove', 'currentlyViewed', function (e) {
            props.map.current.getCanvas().style.cursor = 'pointer';
            var coordinates = e.lngLat;
            const nf = new Intl.NumberFormat();
            var description = "<b>District: "+e.features[0].properties.districtNumber+"</b><br/> Total Population: "+nf.format(e.features[0].properties.TOTPOP)+"<br/> Incumbent(s): "
            +e.features[0].properties.incumbents+"<br/> Hispanic: "+nf.format(e.features[0].properties.HISP)+"<br/> White: "+nf.format(e.features[0].properties.WHITE)
            +"<br/> Black: "+nf.format(e.features[0].properties.BLACK)+"<br/> American Indian/Alaska Native: "+nf.format(e.features[0].properties.AMIN)
            +"<br/> Asian: "+nf.format(e.features[0].properties.ASIAN)+"<br/> Native Hawaiian/Pacific Islander: "+nf.format(e.features[0].properties.NHPI)
            +"<br/> Other: "+nf.format(e.features[0].properties.OTHER)+"<br/> Voting Age Population: "+nf.format(e.features[0].properties.VAP)
            +"<br/> Area: "+nf.format(e.features[0].properties.area)+"<br/> Compactness: "+nf.format(e.features[0].properties.compactness);
            popup.setLngLat(coordinates).setHTML(description).addTo(props.map.current);
            if (e.features.length > 0) {
                if (hoveredStateId) {
                    props.map.current.setFeatureState(
                        { source: 'currentlyViewed', id: hoveredStateId },
                        { hover: false }
                    );
                }
                hoveredStateId = e.features[0].properties.districtNumber;
                props.map.current.setFeatureState(
                    { source: 'currentlyViewed', id: hoveredStateId },
                    { hover: true }
                );
            }
        }); 
        props.map.current.on('mouseleave', 'currentlyViewed', function () {
            props.map.current.getCanvas().style.cursor = '';
            popup.remove();
            if (hoveredStateId) {
                props.map.current.setFeatureState(
                    { source: 'currentlyViewed', id: hoveredStateId },
                    { hover: false }
                );
            }
            hoveredStateId = null;
        });
    }

    return(
        <div className="resultsDiv">
            <div className="metricsDiv">
                <p style={{fontWeight: 'bold'}}>Matching Districtings: {props.getActiveJob()==null?"null":props.getActiveJob().Districtings}</p>
                <table className="resultsTable">
                    <tr>
                        <th>Category</th>
                        <th>Lowest</th>
                        <th>Average</th>
                        <th>Highest</th>
                    </tr>
                    <tr>
                        <td>Objective Score</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().minObjScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().avgObjScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().maxObjScore.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td>Population Equality</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().minPopScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().avgPopScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().maxPopScore.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td>Split Counties</td>
                        <td>--</td>
                        <td>--</td>
                        <td>--</td>
                    </tr>
                    <tr>
                        <td>Deviation (Average)</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().minDevAvgScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().avgDevAvgScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().maxDevAvgScore.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td>Deviation (Enacted)</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().minDevEnaScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().avgDevEnaScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().maxDevEnaScore.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td>Compactness</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().minComScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().avgComScore.toFixed(2)}</td>
                        <td>{getJSONSummary()==null ? null:getJSONSummary().maxComScore.toFixed(2)}</td>
                    </tr>
                    <tr>
                        <td>Political Fairness</td>
                        <td>--</td>
                        <td>--</td>
                        <td>--</td>
                    </tr>
                </table>
                <select className="sortBySelect" onChange={handleSortCatChange}>
                    {populateOptions()}
                </select>
                {/*<select className="sortSelect" onChange={handleSortChange}>
                    <option value="desc">Descending</option>
                    <option value="asc">Ascending</option>
                </select>*/}
            </div>
            <div className="listDiv">
                {populateList()}
            </div>
            <div style={{width: '100%', color: 'white', backgroundColor: 'darkslategray'}}>
                Showing top 10 results.
            </div>
            { !props.reloadData ? null : <div className="loadingOverlay" onClick={()=>props.setReloadData(false)}><img src={loading} alt="loading" style={{position: 'relative', top: '30%', margin: 'auto'}}/></div>}
        </div>
    );
}
export default ResultsTab;