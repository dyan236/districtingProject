import React, { useState } from 'react';
import '../styles/ObjectiveTab.css'
import check from '../resources/greenCheck.png'

//Side-note: Need init() function that asks what pop. data is available and disable pop. eq. buttons appropriately.
function ObjectiveTab(props){
    //State variables & methods
    const [weightPopulationEquality, setPopEqWeight] = useState(props.savedObjectiveOptions[0]>0?props.savedObjectiveOptions[0]:0.5);
    const [weightSplitCounties, setSplitWeight] = useState(props.savedObjectiveOptions[1]>0?props.savedObjectiveOptions[1]:0.5);
    const [weightDeviationAverage, setDevAvgWeight] = useState(props.savedObjectiveOptions[2]>0?props.savedObjectiveOptions[2]:0.5);
    const [weightDeviationEnacted, setDevEnacWeight] = useState(props.savedObjectiveOptions[3]>0?props.savedObjectiveOptions[3]:0.5);
    const [weightCompactness, setCompWeight] = useState(props.savedObjectiveOptions[4]>0?props.savedObjectiveOptions[4]:0.5);
    const [weightPoliticalFairness, setPolFairWeight] = useState(props.savedObjectiveOptions[5]>0?props.savedObjectiveOptions[5]:0.5);
    const handlePopEqChange = event => setPopEqWeight(event.target.value/100);
    const handleSplitChange = event => setSplitWeight(event.target.value/100);
    const handleDevAvgChange = event => setDevAvgWeight(event.target.value/100);
    const handleDevEnacChange = event => setDevEnacWeight(event.target.value/100);
    const handleCompChange = event => setCompWeight(event.target.value/100);
    const handlePolFairChange = event => setPolFairWeight(event.target.value/100);
    const activeJob = props.getActiveJob();
    const feedbackRef=React.createRef();

    //Send weights to server
    const sendServerRequest = event =>{
        if(activeJob===null){
            alert("Please select a job first. ");
            return;
        }
        var params=[weightPopulationEquality, weightSplitCounties, weightDeviationAverage, weightDeviationEnacted, weightCompactness, weightPoliticalFairness];
        props.saveObjectiveFunction(params);
        props.setReloadData(true);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/applyWeights";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE: "+this.responseText);
            }
            else if(this.readyState===4 && this.status===400){
                alert("Please apply constraints first. ");
                return;
            }
        }
        xmlhttp.open("POST", url, true);
        xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        xmlhttp.withCredentials = true;
        xmlhttp.send("params="+JSON.stringify(params));
        feedbackRef.current.style.visibility = 'visible';
        feedbackRef.current.style.opacity = 1;
    }

    //Method for hiding elements
    const hideFeedback = event =>{
        feedbackRef.current.style.opacity = 0;
        feedbackRef.current.style.visibility = 'hidden';
    }

    return(
        <div style={{width: '100%', height: '90%'}}>
            <h1 style={{textAlign: 'center', marginBottom: '0'}}>Current Job: {activeJob==null ? "null" : activeJob.Job} | Districtings: {activeJob==null ? "null" : activeJob.Districtings}
            </h1>
            <div className="centerDiv">
                <label className="sectionalText" style={{padding: '0'}}>Population Equality</label>
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handlePopEqChange} defaultValue={weightPopulationEquality*100}/> <br/>
                    <label>{weightPopulationEquality}</label>
                </div>
                <label className="sectionalText" style={{padding: '0'}}>Split Counties</label>
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handleSplitChange} defaultValue={weightSplitCounties*100}/> <br/>
                    <label>{weightSplitCounties}</label>
                </div>
                <label className="sectionalText" style={{padding: '0'}}>Deviation from Average Districting</label>
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handleDevAvgChange} defaultValue={weightDeviationAverage*100}/> <br/>
                    <label>{weightDeviationAverage}</label>
                </div>
                <label className="sectionalText" style={{padding: '0'}}>Deviation from Enacted Districting</label>
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handleDevEnacChange} defaultValue={weightDeviationEnacted*100}/> <br/>
                    <label>{weightDeviationEnacted}</label>
                </div>
                <label className="sectionalText" style={{padding: '0'}}>Compactness</label>
                {/*<div style={{textAlign: 'left', paddingLeft: '12px', paddingBottom: '12px'}}>
                        <input type="radio" name="chosenCompactness" id="polsby" value="polsby" defaultChecked={true}/> Polsby-Popper <br/>
                        <input type="radio" name="chosenCompactness" id="fatness" value="fatness"/> Population Fatness <br/>
                        <input type="radio" name="chosenCompactness" id="graph" value="graph"/> Graph Compactness <br/>
                </div>*/}
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handleCompChange} defaultValue={weightCompactness*100}/> <br/>
                    <label>{weightCompactness}</label>
                </div>
                <label className="sectionalText" style={{padding: '0'}}>Political Fairness</label>
                <div>
                    <input type="range" min="0" max="100" className="objectiveSlider" onChange={handlePolFairChange} defaultValue={weightPoliticalFairness*100}/> <br/>
                    <label>{weightPoliticalFairness}</label>
                </div>
                <button className="applyButton" onClick={()=>sendServerRequest()} onMouseOut={hideFeedback}>Apply Weights</button>
            </div>
            <div className="applyFeedback" ref={feedbackRef} style={{top: '12px'}}><img src={check} alt="check" style={{position: 'absolute', width: '36px', left: '33%'}}/> Applied weights</div>
        </div>
    );
}
export default ObjectiveTab;