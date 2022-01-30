import React, { useState } from 'react';
import '../styles/FilterTab.css'
import check from '../resources/greenCheck.png'

function FilterTab(props){
    //State variables & methods
    const [showIncumbents, setShowIncumbents] = useState(false);
    const [equalPopulationValue, setEqualPopulationValue] = useState(props.savedFilterOptions[1]>0?props.savedFilterOptions[1]:0.07);
    const [popType, setPopType] = useState(props.savedFilterOptions[2]>0?props.savedFilterOptions[2]:"TOTPOP");
    const [majMinDistricts, setMajMinDistricts] = useState(props.savedFilterOptions[3]>0?props.savedFilterOptions[3]:0);
    const [majMinThreshold, setMajMinThreshold] = useState(props.savedFilterOptions[4]>0?props.savedFilterOptions[4]:0.25);
    const [minority, setMinority] = useState(props.savedFilterOptions[5]>0?props.savedFilterOptions[5]:"HISP");
    const [compactnessValue, setCompactnessValue] = useState(props.savedFilterOptions[6]>0?props.savedFilterOptions[6]:0.5);
    const [allIncumbents, setAllIncumbents] = useState(null);
    const saveFilterOptions = props.saveFilterFunction;
    const activeJob = props.getActiveJob();
    const handleEqPopChange = event => setEqualPopulationValue((event.target.value/1428.57142857).toFixed(4));
    const handleThresholdChange = event => setMajMinThreshold(event.target.value);
    const handleMajMinChange = event => setMajMinDistricts(event.target.value);
    const handleMinorityChange = event => setMinority(event.target.value);
    const handleCompactnessChange = event => setCompactnessValue(event.target.value/100);
    const handleIncumbentChange = event =>{
        //console.log(event.target.checked+" "+(event.target.value==true));
        if(event.target.checked){
            props.listIncumbents.push(event.target.name);
        }
        else{
            props.listIncumbents.splice(props.listIncumbents.indexOf(event.target.name), 1);
        }
    }
    const updateActiveJob = (job) =>{
        props.updateActiveJob(job);
    }
    const feedbackRef=React.createRef();

    //Send constraint parameters to server
    const sendServerRequest = event =>{
        if(activeJob===null){
            alert("Please select a job first. ");
            return;
        }
        var params=[props.listIncumbents, equalPopulationValue, popType, majMinDistricts, majMinThreshold, minority, compactnessValue];
        var paramsString="incumbents="+JSON.stringify(props.listIncumbents);
        paramsString+="&eqPop="+equalPopulationValue;
        paramsString+="&popType="+popType;
        paramsString+="&majMinDistricts="+majMinDistricts;
        paramsString+="&majMinThreshold="+majMinThreshold;
        paramsString+="&minority="+minority;
        paramsString+="&compactness="+compactnessValue;
        //console.log(paramsString);
        saveFilterOptions(params);
        feedbackRef.current.style.visibility = 'visible';
        feedbackRef.current.style.opacity = 1;
        props.setReloadData(true);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/applyConstraints";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                //console.log("HTTPRESPONSE: "+this.responseText);
                var json=JSON.parse(this.responseText);
                updateActiveJob(json);
            }
        }
        xmlhttp.open("POST", url, true);
        xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xmlhttp.withCredentials = true;
        xmlhttp.send(paramsString);
    }

    //Method for hiding element upon hovering out
    const hideFeedback = event =>{
        feedbackRef.current.style.opacity = 0;
        feedbackRef.current.style.visibility = 'hidden';
    }

    const populateIncumbentList = event =>{
        if(allIncumbents!=null){
            return allIncumbents.map((incumbent)=>{
                return <label className="checkboxContainer" key={incumbent.incumbent}>
                    <input type="checkbox" defaultChecked={props.listIncumbents.includes(incumbent.incumbent)} name={incumbent.incumbent} onChange={handleIncumbentChange}/> 
                    <span className="checkmark"></span>
                    {incumbent.incumbent} (District {incumbent.district})
                </label>
            })
        }
        else{
            var xmlhttp=new XMLHttpRequest();
            var url="http://localhost:8080/incumbents";
            xmlhttp.onreadystatechange = function(){
                if(this.readyState===4 && this.status===200){
                    //console.log(props.selectedUSState+", HTTPRESPONSE: "+this.responseText);
                    var json=JSON.parse(this.responseText);
                    setAllIncumbents(json);
                }
            }
            xmlhttp.open("GET", url);
            xmlhttp.withCredentials = true;
            xmlhttp.send();
        }
    }

    //Help text stuff
    const helpText1=React.createRef();
    const showHelp1 = () =>{
        helpText1.current.style.visibility="visible";
        helpText1.current.style.opacity=1;
    }
    const hideHelp1 = () =>{
        helpText1.current.style.opacity=0;
        helpText1.current.style.visibility="hidden";
    }
    const helpText2=React.createRef();
    const showHelp2 = () =>{
        helpText2.current.style.visibility="visible";
        helpText2.current.style.opacity=1;
    }
    const hideHelp2 = () =>{
        helpText2.current.style.opacity=0;
        helpText2.current.style.visibility="hidden";
    }
    const helpText3=React.createRef();
    const showHelp3 = () =>{
        helpText3.current.style.visibility="visible";
        helpText3.current.style.opacity=1;
    }
    const hideHelp3 = () =>{
        helpText3.current.style.opacity=0;
        helpText3.current.style.visibility="hidden";
    }
    const helpText4=React.createRef();
    const showHelp4 = () =>{
        helpText4.current.style.visibility="visible";
        helpText4.current.style.opacity=1;
    }
    const hideHelp4 = () =>{
        helpText4.current.style.opacity=0;
        helpText4.current.style.visibility="hidden";
    }
    
    return(
        <div style={{width: '100%', height: '90%'}}>
            <h1 style={{textAlign: 'center'}}>Current Job: {activeJob==null ? "null" : activeJob.Job} | Districtings: {activeJob==null ? "null" : activeJob.Districtings}
            </h1>
            <div className="filterDiv">
                { !showIncumbents ? null: <div className="incumbentWindow">
                        <div className="incumbentBackground">
                            <div style={{position: 'relative'}}>
                                <h1>Incumbents</h1>
                                <p className="incumbentDesc">Select those you would like to see retained.</p>
                                <button className="backButton" onClick={()=>setShowIncumbents(false)}>Back</button>
                            </div>
                            <div className="checkboxListContainer">
                                {populateIncumbentList()}
                            </div>
                        </div>
                    </div>
                }
                <label className="sectionalText" style={{textAlign: 'left', paddingBottom: '0'}}>Incumbent Protection <label style={{color: 'blue'}} onMouseOver={showHelp1} onMouseOut={hideHelp1}>(?)</label></label>
                <button className='filterButton' onClick={()=>setShowIncumbents(true)}>View Incumbents</button>
                <label className="sectionalText" style={{textAlign: 'left'}}>Equal Population <label style={{color: 'blue'}} onMouseOver={showHelp2} onMouseOut={hideHelp2}>(?)</label></label>
                <div>
                    <div style={{textAlign: 'left', paddingLeft: '12px', paddingBottom: '12px'}}>
                        <input type="radio" name="chosenPopulation" id="TOTPOP" value="TOTPOP" defaultChecked={true} onClick={()=>setPopType("TOTPOP")}/> Total Population <br/>
                        <input type="radio" name="chosenPopulation" id="VAP" value="VAP" onClick={()=>setPopType("VAP")}/> Voting Age Population (TVAP) <br/>
                        <input type="radio" name="chosenPopulation" id="CVAP" value="CVAP" disabled/> <label style={{color: 'gray'}}>Citizen Voting Age Population (CVAP) </label><br/>
                    </div>
                    <input type="range" min="0" max="100" className="filterSlider" onChange={handleEqPopChange} defaultValue={equalPopulationValue*1428.57142857}/> <br/>
                    <label>{equalPopulationValue}%</label>
                </div>
                <label className="sectionalText" style={{textAlign: 'left'}}>Majority-Minority Districts <label style={{color: 'blue'}} onMouseOver={showHelp3} onMouseOut={hideHelp3}>(?)</label></label>
                <div style={{textAlign: 'left', paddingBottom: '12px'}}>
                    &emsp;Minimum:&emsp;<input type="number" style={{width: '15%'}} min="0" max="8" onChange={handleMajMinChange} defaultValue={majMinDistricts}/><br/>
                    &emsp;Threshold:&emsp;<input type="number" style={{width: '15%'}} min="0" max="1" step="0.01" onChange={handleThresholdChange} defaultValue={majMinThreshold}/><br/>
                    &emsp;Minority:&ensp;&emsp;<select default={minority} onChange={handleMinorityChange}>
                        <option value="HISP">Hispanic/Latino</option>
                        <option value="WHITE">White</option>
                        <option value="BLACK">Black</option>
                        <option value="AMIN">American Indian/Alaska Native</option>
                        <option value="ASIAN">Asian</option>
                        <option value="NHPI">Native Hawaiian/Pacific Islander</option>
                        <option value="OTHER">Other</option>
                    </select>
                </div>
                <label className="sectionalText" style={{textAlign: 'left'}}>Compactness <label style={{color: 'blue'}} onMouseOver={showHelp4} onMouseOut={hideHelp4}>(?)</label></label>
                <div style={{textAlign: 'left', paddingLeft: '12px', paddingBottom: '12px'}}>
                    <input type="radio" name="chosenCompactness" id="graph" value="graph" disabled/><label style={{color: 'gray'}}> Graph Compactness </label><br/>
                    <input type="radio" name="chosenCompactness" id="fatness" value="fatness" disabled/><label style={{color: 'gray'}}> Population Fatness </label><br/>
                    <input type="radio" name="chosenCompactness" id="polsby" value="polsby" defaultChecked={true}/> Polsby-Popper <br/>
                </div>
                <div>
                    <input type="range" min="0" max="100" className="filterSlider" onChange={handleCompactnessChange} defaultValue={compactnessValue*100}/> <br/>
                    <label>{compactnessValue}</label>
                </div>
                <button className="applyButton" onClick={()=>sendServerRequest()} onMouseOut={hideFeedback}>Apply Constraints</button>
            </div>
            <div className="applyFeedback" ref={feedbackRef}><img src={check} alt="check" style={{position: 'absolute', width: '36px', left: '35%'}}/> Applied filter</div>
            <div className="hintDiv" ref={helpText1} style={{top: '185px'}}><label className="hint">Districtings that do not maintain the selected incumbents will be filtered out. </label></div>
            <div className="hintDiv" ref={helpText2} style={{top: '245px'}}><label className="hint">Districtings with a disparity in population higher than specified will be filtered out. </label></div>
            <div className="hintDiv" ref={helpText3} style={{top: '405px'}}><label className="hint">Districtings with less than minimum will be filtered out. </label></div>
            <div className="hintDiv" ref={helpText4} style={{top: '520px'}}><label className="hint">Districtings with compactness lower than specified will be filtered out. </label></div>
        </div>
    );
}
export default FilterTab;