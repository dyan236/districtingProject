import React, { useState } from 'react';
import Plotly from 'plotly.js';
import createPlotlyComponent from 'react-plotly.js/factory';
import '../styles/DetailedTab.css'

function DetailedTab(props){
    const Plot=createPlotlyComponent(Plotly);
    const [currentDistricting, setCurrentDistricting] = useState(props.getCurrentFunction());
    const [viewPlot, setViewPlot] = useState(false);
    const [viewDetails, setViewDetails] = useState(false);
    const [detailTitle, setDetailTitle] = useState("");
    const [detailRows, setDetailRows] = useState(null);
    const [detailArray, setDetailArray] = useState(null);
    const [plotData, setPlotData] = useState(null);
    const [devEnactedDetails, setDevEnactedDetails] = useState(null);
    const [minorityDetail, setMinorityDetail] = useState(null);
    const getDistrictingSummary = () =>{
        return props.getDistrictingSummary();
    }
    const getObjectiveOptions = () =>{
        return props.savedObjectiveOptions;
    }
    //var plot_data=require('../data/generated_plot.json');   //TODO: remove

    const format = (number) =>{
        const nf = new Intl.NumberFormat();
        return nf.format(number);
    }

    const format2 = (number) =>{
        const nf = new Intl.NumberFormat();
        if(number>1) return nf.format(number);
        else return number.toFixed(5);
    }
    
    const requestPopScore = () =>{
        setDevEnactedDetails(null);
        setDetailRows(null);
        setDetailArray(null);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/popScore";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setDetailRows(json.rows);
                setDetailArray(json.data);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        setDetailTitle("Population Score Breakdown");
        setViewDetails(true);
    }

    const requestDevAvgScore = () =>{
        setDevEnactedDetails(null);
        setDetailRows(null);
        setDetailArray(null);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/devAvg";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setDetailRows(json.rows);
                setDetailArray(json.data);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        setDetailTitle("Deviation(Average) Score Breakdown");
        setViewDetails(true);
    }

    const requestDevEnaScore = () =>{
        setDevEnactedDetails(null);
        setDetailRows(null);
        setDetailArray(null);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/devEna";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setDetailRows(json.details.rows);
                setDetailArray(json.details.data);
                setDevEnactedDetails(json);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        setDetailTitle("Deviation(Enacted) Score Breakdown");
        setViewDetails(true);
    }

    const requestCompactness = () =>{
        setDevEnactedDetails(null);
        setDetailRows(null);
        setDetailArray(null);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/compactness";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setDetailRows(json.rows);
                setDetailArray(json.data);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        setDetailTitle("Compactness Score Breakdown");
        setViewDetails(true);
    }

    const createTable = () =>{
        if(detailRows!=null && detailArray!=null){
            return detailRows.map((row, index) =>{
                if(index===detailRows.length-1)
                    return <tr><td style={{textAlign: 'left', paddingLeft: '12px', paddingRight: '12px'}}><b>{row}</b></td><td style={{textAlign: 'left', paddingLeft: '12px', paddingRight: '12px'}}><b>{format2(detailArray[index++])}</b></td></tr>
                else
                    return <tr><td style={{textAlign: 'left', paddingLeft: '12px', paddingRight: '12px'}}>{row}</td><td style={{textAlign: 'left', paddingLeft: '12px', paddingRight: '12px'}}>{format2(detailArray[index++])}</td></tr>
            });
        }
        else return null;
    }

    const createHeaders = ()=>{
        if(devEnactedDetails!=null){
            return devEnactedDetails.headers.map((header) =>{
                return <th>{header}</th>
            });
        }
        else return null;
    }

    const fillData = () =>{
        if(devEnactedDetails!=null){
            return devEnactedDetails.data.map((row) =>{
                return <tr>
                    <td>{format(row[0])}</td>
                    <td>{format(row[1])}</td>
                    <td>{format(row[2])}</td>
                    <td>{format(row[3])}</td>
                    <td>{format(row[4])}</td>
                </tr>
            });
        }
        else return null;
    }

    const createTable2 = () =>{
        if(devEnactedDetails!=null){
            return <table>
                <tr>
                    {createHeaders()}
                </tr>
                {fillData()}
            </table>
        }
        else return null;
    }

    const createHeaders2 = ()=>{
        if(minorityDetail!=null){
            return minorityDetail.headers.map((header) =>{
                return <th>{header}</th>
            });
        }
        else return null;
    }

    const fillData2 = () =>{
        if(minorityDetail!=null){
            return minorityDetail.data.map((row) =>{
                return <tr>
                    <td>{format(row[0])}</td>
                    <td>{format(row[1])}</td>
                    <td>{format(row[2])}</td>
                    <td>{format(row[3])}</td>
                </tr>
            });
        }
        else return null;
    }

    const createTable3 = () =>{
        if(minorityDetail!=null){
            return <table>
                <tr>
                    {createHeaders2()}
                </tr>
                {fillData2()}
            </table>
        }
        else return null;
    }

    const requestMinorityDetail = () =>{
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/minorityDetail";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setMinorityDetail(json);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
    }

    const requestPlotData = () =>{
        setMinorityDetail(null);
        setPlotData(null);
        var xmlhttp=new XMLHttpRequest();
        var url="http://localhost:8080/details/boxandwhisker";
        xmlhttp.onreadystatechange = function(){
            if(this.readyState===4 && this.status===200){
                var json=JSON.parse(this.responseText);
                //console.log("HTTPRESPONSE(200): "+this.responseText);
                setPlotData(json);
            }
        }
        xmlhttp.open("GET", url, true);
        xmlhttp.withCredentials = true;
        xmlhttp.send();
        setViewPlot(true);
        requestMinorityDetail();
    }

    const getPlotData = () =>{
        if(plotData!=null){
            var arr=[];
            for(var i=0;i<plotData.data.length;i++){
                arr[i]={name: (i+1), y: plotData.data[i], type: 'box', fillcolor: 'rgba(0,0,0,0)', marker: {color: 'black'}, showlegend: false, boxpoints: false};
            }
            var xAxis=[];
            for(i=1;i<=plotData.data.length;i++)
                xAxis.push(i);
            arr.push({name: 'currently viewed districting', x: xAxis, y: plotData.current, mode: 'markers', type: 'scatter', marker: {color: 'yellow', line: {width: 1}}});
            arr.push({name: 'currently enacted districting', x: xAxis, y: plotData.enacted, mode: 'markers', type: 'scatter', marker: {color: 'green', line: {width: 1}}});
            return arr;
        }
        else return [];
    }

    return(
        <div>
            {currentDistricting===null ? <h1 className="sectionalText" style={{textAlign: 'center'}}>No districting currently selected. <br/>Click "View" on a districting in the results tab. </h1> :
                <div className="detailedContainer">
                    <div style={{width: '100%', backgroundColor: 'darkslategray', borderBottom: 'solid 1px'}}>
                        <table>
                            <tr>
                                <th className="sectionalTh">Districting:</th><th className="sectionalTh">{currentDistricting.index}</th>
                            </tr>
                            <tr>
                                <th className="sectionalTh">Objective Score:</th><th className="sectionalTh">{currentDistricting.objScore.toFixed(2)}</th>
                            </tr>
                        </table>
                    </div>
                    <h1 className="sectionalText" style={{textAlign: 'center'}}>Objective Details</h1>
                    <table>
                        <tr>
                            <td className="detailedTd">Population Equality Score:</td>
                            <td className="detailedTd">{format(currentDistricting.popScore*100)}% * ({getObjectiveOptions()[0]})</td>
                            <td className="detailedTd"><button onClick={()=>requestPopScore()}>More Details</button></td>
                        </tr>
                        <tr>
                            <td className="detailedTd">Split County Score:</td>
                            <td className="detailedTd">--</td>
                            <td className="detailedTd"><button disabled>More Details</button></td>
                        </tr>
                        <tr>
                            <td className="detailedTd">Deviation (Average) Score:</td>
                            <td className="detailedTd">{format(currentDistricting.deviationAverage*100)}% * ({getObjectiveOptions()[2]})</td>
                            <td className="detailedTd"><button onClick={()=>requestDevAvgScore()}>More Details</button></td>
                        </tr>
                        <tr>
                            <td className="detailedTd">Deviation (Enacted) Score:</td>
                            <td className="detailedTd">{format(currentDistricting.deviationEnacted*100)}% * ({getObjectiveOptions()[3]})</td>
                            <td className="detailedTd"><button onClick={()=>requestDevEnaScore()}>More Details</button></td>
                        </tr>
                        <tr>
                            <td className="detailedTd">Compactness Score:</td>
                            <td className="detailedTd">{format(currentDistricting.compactnessScore*100)}% * ({getObjectiveOptions()[4]})</td>
                            <td className="detailedTd"><button onClick={()=>requestCompactness()}>More Details</button></td>
                        </tr>
                        <tr>
                            <td className="detailedTd">Political Fairness Score:</td>
                            <td className="detailedTd">--</td>
                            <td className="detailedTd"><button disabled>More Details</button></td>
                        </tr>
                    </table>
                    <h1 className="sectionalText" style={{textAlign: 'center', borderTop: 'solid 1px'}}>Districting Details</h1>
                    <div style={{paddingBottom: '24px'}}>
                        <table>
                            <tr><td className="detailedTd">Total districts:</td><td className="detailedTd">{getDistrictingSummary()==null ? null :format(getDistrictingSummary()[0])}</td></tr>
                            <tr><td className="detailedTd">Total population:</td><td className="detailedTd">{getDistrictingSummary()==null ? null :format(getDistrictingSummary()[1])}</td></tr>
                            <tr><td className="detailedTd">Voting age population:</td><td className="detailedTd">{getDistrictingSummary()==null ? null :format(getDistrictingSummary()[2])}</td></tr>
                            <tr><td className="detailedTd">Minority population:</td><td className="detailedTd">{getDistrictingSummary()==null ? null :format(getDistrictingSummary()[3])}</td></tr>
                            <tr><td className="detailedTd">Majority-minority Districts:</td><td className="detailedTd">{getDistrictingSummary()==null ? null :format(getDistrictingSummary()[4])}</td></tr>
                        </table>
                    </div>
                    <button className="plotButton" onClick={()=>{requestPlotData()}}>View Minority Comparison Plot</button>
                </div>
            }
            { !viewPlot ? null :
            <div className="plotContainer">
                <div className="plotBackground">
                    <h1 className="sectionalText" style={{paddingLeft: '12px'}}>Minority Comparison Box and Whisker</h1>
                    <Plot
                        data={getPlotData()}
                        layout={ {width: 800, height: 500, plot_bgcolor: 'rgba(0,0,0,0)', paper_bgcolor: 'rgba(0,0,0,0.1)', xaxis: {title:{text:'Indexed District'}, tickformat: ',d', showline: true}, yaxis:{title:{text:'% Minority'}, showline: true}, legend: {xanchor: 'right', y: -.2, bgcolor: 'rgba(0,0,0,0)'}} }
                    />
                    <h1/>
                    {createTable3()}
                    <h1/>
                    <button className="backButtonPlot" onClick={()=>{setViewPlot(false)}}>Back</button>
                </div>
            </div>
            }
            {!viewDetails ? null :
                <div className="plotContainer">
                    <div className="detailBackground">
                        <h1 className="sectionalText" style={{textAlign:'center'}}>{detailTitle}</h1>
                        {createTable2()}
                        {devEnactedDetails==null ? null : <h1/>}
                        <table>
                            {createTable()}
                        </table>
                        <h1/>
                        <button className="backButtonPlot" onClick={()=>{setViewDetails(false)}}>Back</button>
                    </div>
                </div>
            }
        </div>
    );
}
export default DetailedTab;