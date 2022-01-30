import React, { useState } from 'react';
import '../styles/Sidebar.css'
import FilterTab from './FilterTab.jsx'
import ObjectiveTab from './ObjectiveTab.jsx'
import ResultsTab from './ResultsTab.jsx'
import DetailedTab from './DetailedTab.jsx'
import loadingGIF from '../resources/loading.gif'

function Sidebar(props){
    //State variables & methods
    const [activeTab, setActiveTab]=useState('Filter');
    const [showSide, setShowSide]=useState(true);
    const [jobWindow, setJobWindow]=useState(false);
    const [activeJob, setActiveJob]=useState(null);
    const [loading, setLoading]=useState(false);
    const [JSONSummary, setJSONSummary]=useState(null);
    const [JSONListing, setJSONListing]=useState(null);
    const [districtingSummary, setDistrictingSummary]=useState(null);
    const getJSONSummary = () => { return JSONSummary; }
    const getJSONListing = () => { return JSONListing; }
    const setJSONListing2 = (arr) => { setJSONListing(arr); }
    const getDistrictingSummary = () =>{ return districtingSummary; }

    const selectJob = (job) =>{
        //setActiveJob(job);
        setLoading(true);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/job?jobid="+job.id;
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var jobjson=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setActiveJob(jobjson);
                setLoading(false);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        //TODO: Parse job JSON
        setJobWindow(false);
    }

    const getActiveJob = () =>{
        return activeJob;
    }

    const updateActiveJob = (job) =>{
        setActiveJob(job);
    }

    const populateOptions = () =>{
        var data=props.getJobSummaries();
        if(data==null) return;
        return data.map((job)=>{
            //return <option>Districting Job {job.job} ({job.districtings} districtings)</option>
            return <div className="jobOption" onClick={()=>selectJob(job)}><b>Job[{job.id}]</b><br/><table>
                <tr><td style={{width: '400px', textAlign: 'left'}}><b>State: </b>{job.state}</td><td style={{width: '400px', textAlign: 'left'}}><b>Districtings: </b>{job.numDistrictings}</td></tr>
                <tr><td colSpan="2" style={{textAlign: 'left'}}><b>MGGG Params: </b>{job.params}</td></tr>
            </table></div>
        });
    }

    //Perform cleanup
    const rechooseState = () =>{
        if (props.map.current.getLayer('currentlyEnacted')) {
            props.map.current.removeLayer('currentlyEnacted');
        }
        if (props.map.current.getSource('currentlyEnacted')) {
            props.map.current.removeSource('currentlyEnacted');
        }
        if (props.map.current.getLayer('precinctLines')) {
            props.map.current.removeLayer('precinctLines');
        }
        if (props.map.current.getSource('precinctLines')) {
            props.map.current.removeSource('precinctLines');
        }
        if (props.map.current.getLayer('currentlyViewed')) {
            props.map.current.removeLayer('currentlyViewed');
        }
        if (props.map.current.getSource('currentlyViewed')) {
            props.map.current.removeSource('currentlyViewed');
        }
        props.setUSState("");
        props.setReloadData(true);
    }

    //Show jobs
    const jobs = () =>{
        setJobWindow(true);
    }

    const retrieveResults = () =>{
        setActiveTab('Results');
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/results";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                setJSONSummary(json);
                //console.log("HTTPRESPONSE: "+json);
                props.setReloadData(false);
                queryTopTen();
            }
            else if(this.readyState===4 && this.status===400){
                alert("Please apply weights first. ");
                props.setReloadData(false);
                return;
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
    }

    const queryTopTen = () =>{
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/results/top";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                setJSONListing(json);
                //console.log("HTTPRESPONSE: "+json);
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

    const retrieveDetails = () =>{
        setActiveTab('Other');
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                setDistrictingSummary(json);
                //console.log("HTTPRESPONSE: "+json);
            }
            else if(this.readyState===4 && this.status===400){
                alert("Please select a districting first. ");
                return;
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
    }

    return(
        <div>
            { !showSide ? <button className="showButton" onClick={()=>setShowSide(true)}>&lt;</button> :
            <div className="sideWindow">
                <div className="sidebarBackground"></div>
                <div className="sideContainer">
                    <h1 className="titleText">{props.selectedUSState}</h1>
                    {/*<select className="jobSelect">
                        {populateOptions()}
                    </select>*/}
                    <div className="tabBar">
                        <button className={activeTab==="Filter" ? 'tabButtonActive' : 'tabButton' } onClick={() => setActiveTab('Filter')}>Constraints</button>
                        <button className={activeTab==="Objective" ? 'tabButtonActive' : 'tabButton' } onClick={() => setActiveTab('Objective')}>Objective Function</button>
                        <button className={activeTab==="Results" ? 'tabButtonActive' : 'tabButton' } onClick={() => retrieveResults()}>Results</button>
                        <button className={activeTab==="Other" ? 'tabButtonActive' : 'tabButton' } onClick={() => retrieveDetails()}>Detailed</button>
                    </div>
                    <button className="rechooseButton" style={{left: '12px'}} onClick={()=>rechooseState()}>&lt; Choose Another State</button>
                    <button className="jobButton" onClick={()=>jobs()}>Select Job</button>
                    <button className="hideButton" onClick={()=>setShowSide(false)}>&gt;</button>
                    <div className="sidebarContainer">
                        {activeTab==="Filter" ? <FilterTab saveFilterFunction={props.saveFilterFunction} savedFilterOptions={props.savedFilterOptions}
                        listIncumbents={props.listIncumbents} setListIncumbents={props.setListIncumbents} setReloadData={props.setReloadData} getActiveJob={getActiveJob}
                        updateActiveJob={updateActiveJob} selectedUSState={props.selectedUSState}></FilterTab> : null}
                        {activeTab==="Objective" ? <ObjectiveTab saveObjectiveFunction={props.saveObjectiveFunction} savedObjectiveOptions={props.savedObjectiveOptions}
                        setReloadData={props.setReloadData} getActiveJob={getActiveJob}></ObjectiveTab> : null}
                        {activeTab==="Results" ? <ResultsTab changeCurrentFunction={props.changeCurrentFunction} map={props.map} setMapFiltering={props.setMapFiltering}
                        reloadData={props.reloadData} setReloadData={props.setReloadData} getActiveJob={getActiveJob} getJSONSummary={getJSONSummary}
                        getJSONListing={getJSONListing} setJSONListing={setJSONListing2} getFilterOptions={props.getFilterOptions} mapboxgl={props.mapboxgl}></ResultsTab> : null}
                        {activeTab==="Other" ? <DetailedTab getCurrentFunction={props.getCurrentFunction} savedObjectiveOptions={props.savedObjectiveOptions} 
                        getDistrictingSummary={getDistrictingSummary}></DetailedTab> : null}
                    </div>
                </div>
                { !loading ? null : <div className="loadingOverlay2"><img src={loadingGIF} alt="loading" 
                style={{position: 'relative', top: '40%', left: '37.5%'}}/></div>}
            </div>
            }
            { !jobWindow ? null : <div className='incumbentWindow'>
                <div className="incumbentBackground" style={{width: '50%'}}>
                    <h1>Available Jobs</h1>
                    <div style={{height: '85%', overflow: 'auto'}}>
                        {populateOptions()}
                    </div>
                </div>
            </div>}
        </div>
    );
}

export default Sidebar;