//import mapboxGlCsp from 'mapbox-gl/dist/mapbox-gl-csp';
import React, { useState } from 'react';
import '../styles/MapFilter.css'

function MapFilter(props){
    const map=props.map;

    const toggleVisibility = (layerId) =>{
        var visible = map.current.getLayoutProperty(layerId, 'visibility')==='visible';
        if(visible)
            map.current.setLayoutProperty(layerId, 'visibility', 'none');
        else
            map.current.setLayoutProperty(layerId, 'visibility', 'visible');
    } 

    const populateFilter = (layer) =>{
        if(layer.type==="symbol"){
            return <label className="checkboxContainerMapFilter">
                <input type="checkbox" defaultChecked={map.current.getLayoutProperty(layer.id, 'visibility')==='visible'} onChange={()=>toggleVisibility(layer.id)}/> 
                <span className="checkmarkMapFilter"></span>
                {layer.id}
            </label>;
        }
    };
    
    return(
        <div className="mapFilterDiv">
            {map.current.getStyle().layers.map((layer)=>populateFilter(layer))}
            <label className="checkboxContainerMapFilter">
                <input type="checkbox" defaultChecked={map.current.getLayoutProperty('precinctLines', 'visibility')==='visible'} onChange={()=>toggleVisibility('precinctLines')}/> 
                <span className="checkmarkMapFilter"></span>
                precinct lines
            </label>
            <label style={{color: 'gray'}} className="checkboxContainerMapFilter">
                <input type="checkbox" disabled/> 
                <span className="checkmarkMapFilter"></span>
                county lines
            </label>
            <label className="checkboxContainerMapFilter">
                <input type="checkbox" defaultChecked={map.current.getLayoutProperty('currentlyEnacted', 'visibility')==='visible'} onChange={()=>toggleVisibility('currentlyEnacted')}/> 
                <span className="checkmarkMapFilter"></span>
                show currently enacted districting
            </label>
            {!map.current.getLayer('currentlyViewed') ? null : <label className="checkboxContainerMapFilter">
                <input type="checkbox" defaultChecked={map.current.getLayoutProperty('currentlyViewed', 'visibility')==='visible'} onChange={()=>toggleVisibility('currentlyViewed')}/> 
                <span className="checkmarkMapFilter"></span>
                show currently viewed districting
            </label>}
        </div>
    )
}
export default MapFilter;