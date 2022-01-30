package com.example.demo;

import com.example.demo.DB.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import com.google.gson.Gson;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins="http://localhost:3000", allowCredentials="true")
@SpringBootApplication
@RestController
public class DemoApplication {
	@Autowired
	private DistrictingPrecomputedRepo precomputeRepo;
	@Autowired
	private IncumbentRepo incumbentRepo;
	@Autowired
	private JobDescriptionRepo jobRepo;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}
	
	public static HttpSession getSession() {
		ServletRequestAttributes serv=(ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		return serv.getRequest().getSession(true);
	}
	
	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}
	
	@GetMapping("/state")
	public List<JobDescription> state(@RequestParam("statename") String statename) {
		/*String res="[{\"id\": 1, \"state\": \"Arkansas\", \"districtings\": 10000, \"params\": \"-g ./src/data/arkansas.json -c 20 -r 10000\"}, "
			+"{\"id\": 2, \"state\": \"Arkansas\", \"districtings\": 200, \"params\": \"-g ./src/data/arkansas.json -c 20\"}, "
			+"{\"id\": 3, \"state\": \"Arkansas\", \"districtings\": 2000, \"params\": \"-g ./src/data/arkansas.json -r 2000\"}, "
			+"{\"id\": 4, \"state\": \"Arkansas\", \"districtings\": 10000, \"params\": \"-g ./src/data/arkansas.json -c 50 -r 10000\"}, "
			+"{\"id\": 5, \"state\": \"Arkansas\", \"districtings\": 5000, \"params\": \"-g ./src/data/arkansas.json -c 40 -r 5000\"}]";*/
		getSession().setAttribute("statename", statename);
		return jobRepo.retrieveForState(statename);
	}
	
	@GetMapping("/job")
	public String job(@RequestParam("jobid") String id) {
		try {
			String statename = (String) getSession().getAttribute("statename");
			File file = new ClassPathResource("data/"+statename+"/"+id+"/"+id+".json").getFile();
			FileReader fReader = new FileReader(file);
			Gson g = new Gson();
			Job j = g.fromJson(fReader, Job.class);
			fReader.close();
			getSession().setAttribute("precomputeRepo", precomputeRepo);
			
			//Load incumbent data
			IncumbentManager manager=new IncumbentManager();
			manager.setIncumbents(incumbentRepo.retrieveForState(statename));
			getSession().setAttribute("Incumbents", manager);
			
			//Load precinct data
			file = new ClassPathResource("data/"+statename+"/"+statename+"-Geometry.json").getFile();
			fReader = new FileReader(file);
			getSession().setAttribute("FeatureCollection", g.fromJson(fReader, FeatureCollection.class));
			fReader.close();
			
			//Load enacted data
			file = new ClassPathResource("data/"+statename+"/"+statename+"-Enacted.geojson").getFile();
			fReader = new FileReader(file);
			getSession().setAttribute("enacted", g.fromJson(fReader, EnactedDistricting.class));
			fReader.close();
			
			getSession().setAttribute("JobID", id);
			j.setSession(getSession());
			j.init();
			getSession().setAttribute("Job", j);
			return "{\"Job\":\""+id+"\", \"Districtings\":"+j.size()+"}";
		}
		catch(Exception e) { e.printStackTrace(); throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.toString()); }
	}
	
	@PostMapping("/applyConstraints")
	public String applyConstraints(@RequestParam("incumbents") String incumbents, @RequestParam("eqPop") double eqPop, @RequestParam("popType") String popTypeString, @RequestParam("majMinDistricts") int majMinDistrict, 
			@RequestParam("majMinThreshold") double threshold, @RequestParam("minority") String minority, @RequestParam("compactness") double compactness) {
		Gson g=new Gson();
		String[] parsedIncumbents=g.fromJson(incumbents, String[].class);
		Job j=(Job) getSession().getAttribute("Job");
		Minority minorityType;
		switch(minority) {
			case "HISP":
				minorityType=Minority.HISP;
				break;
			case "WHITE":
				minorityType=Minority.WHITE;
				break;
			case "BLACK":
				minorityType=Minority.BLACK;
				break;
			case "AMIN":
				minorityType=Minority.AMIN;
				break;
			case "ASIAN":
				minorityType=Minority.ASIAN;
				break;
			case "NHPI":
				minorityType=Minority.NHPI;
				break;
			case "OTHER":
				minorityType=Minority.OTHER;
				break;
			default:
				minorityType=Minority.HISP;
		}
		int popType=0;
		if(popTypeString.equals("VAP")) popType=1;
		FilteredJob fJob=j.applyConstraints(parsedIncumbents, majMinDistrict, minorityType, threshold, compactness, popType, eqPop);
		getSession().setAttribute("FilteredJob", fJob);
		return "{\"Job\":\""+getSession().getAttribute("JobID")+"\", \"Districtings\":"+fJob.getPlans().size()+"}";
	}
	
	@GetMapping("/incumbents")
	public List<Incumbent> incumbents() {
		String statename=(String) getSession().getAttribute("statename");
		try{
			return incumbentRepo.retrieveForState(statename);
		}
		catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.toString());
		}
	}
	
	@PostMapping("/applyWeights")
	public String applyWeights(@RequestParam("params") String paramsString) {
		try {
			Gson g = new Gson();
			double[] weights=g.fromJson(paramsString, double[].class);
			FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
			fJob.applyWeights(weights);
			getSession().setAttribute("FilteredJob", fJob);
			return "{\"Job\":\""+getSession().getAttribute("JobID")+"\", \"Districtings\":"+fJob.size()+"}";
		}
		catch(Exception e) { e.printStackTrace(); throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString()); }
	}
	
	@GetMapping("/precincts")
	public String precincts(@RequestParam("statename") String statename) {
		try{
			File data=new ClassPathResource("data/"+statename+"/"+statename+"-Geometry.json").getFile();
			return new String(Files.readAllBytes(data.toPath()));
		}
		catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.toString());
		}
	}
	
	@GetMapping("/enacted")
	public String enacted(@RequestParam("statename") String statename) {
		try{
			File data=new ClassPathResource("data/"+statename+"/"+statename+"-Enacted.geojson").getFile();
			FileReader fReader=new FileReader(data);
			Gson g=new Gson();
			EnactedDistricting enacted=g.fromJson(fReader, EnactedDistricting.class);
			getSession().setAttribute("EnactedDistricting", enacted);
			return new String(Files.readAllBytes(data.toPath()));
		}
		catch(Exception e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.toString());
		}
	}
	
	@GetMapping("/results")
	public String results() {
		try {
			FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
			Gson g=new Gson();
			return g.toJson(fJob.generateSummary(), JobSummary.class);
		}
		catch(Exception e) { e.printStackTrace(); throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString()); }
	}
	
	@GetMapping("/results/top")
	public Iterable<Districting> top() {
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		ArrayList<Districting> tmp=fJob.getTopTen();
		for(Districting d:tmp)
			d.setMajMinDistricts(d.getNumMajMinDistricts(fJob.getMMThresh(), fJob.getMinority()));
		return tmp;
	}
	
	@GetMapping("/view")
	public String view(@RequestParam("districtingId") int index) {
		try{
			Job j=(Job) getSession().getAttribute("Job");
			getSession().setAttribute("currentDistricting", j.getDistricting(index));
			ViewDistricting v=j.constructGeometry(index);
			Gson g=new Gson();
			return g.toJson(v, ViewDistricting.class);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString());
		}
	}
	
	@GetMapping("/details")
	public double[] details() {
		try {
			Districting current=(Districting) getSession().getAttribute("currentDistricting");
			double[] res=new double[5];
			res[0]=current.getNumDistricts();
			res[1]=current.getTOTPOP();
			res[2]=current.getVAP();
			FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
			Minority minority=fJob.getMinority();
			double MMThresh=fJob.getMMThresh();
			res[3]=current.getMinorityCount(minority);
			res[4]=current.getNumMajMinDistricts(MMThresh, minority);
			return res;
		}
		catch(Exception e) { e.printStackTrace(); throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.toString()); }
	}
	
	@GetMapping("details/popScore")
	public String showPopScore() {
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		CalculationDetails calcs = current.showPopScore();
		calcs.addMinMax(fJob.getRecord(0)[0], fJob.getRecord(0)[1]);
		calcs.addFinal(current.getPopScore());
		Gson g=new Gson();
		return g.toJson(calcs, CalculationDetails.class);
	}
	
	@GetMapping("details/devAvg")
	public String showDeviationAverage() {
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		CalculationDetails calcs = current.showDevAvgScore(fJob.getAverageDistricting(), fJob.getMinority());
		calcs.addMinMax(fJob.getRecord(1)[0], fJob.getRecord(1)[1]);
		calcs.addFinal(current.getDeviationAverage());
		Gson g=new Gson();
		return g.toJson(calcs, CalculationDetails.class);
	}
	
	@GetMapping("details/devEna")
	public String showDeviationEnacted() {
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		DeviationEnactedDetails calcs = current.showDevEnaScore(fJob.getEnactedDistricting());
		calcs.getDetails().addMinMaxArea(fJob.getRecord(2)[0], fJob.getRecord(2)[1]);
		calcs.getDetails().addMinMaxPop(fJob.getRecord(3)[0], fJob.getRecord(3)[1]);
		calcs.getDetails().addFinalArea(current.getDeviationEnactedArea());
		calcs.getDetails().addFinalPop(current.getDeviationEnactedPop());
		calcs.getDetails().addFinal(current.getDeviationEnacted());
		Gson g=new Gson();
		return g.toJson(calcs, DeviationEnactedDetails.class);
	}
	
	@GetMapping("details/compactness")
	public String showCompactness() {
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		CalculationDetails calcs = current.showCompactness();
		calcs.addMinMax(fJob.getRecord(4)[0], fJob.getRecord(4)[1]);
		calcs.addFinal(current.getCompactnessScore());
		Gson g=new Gson();
		return g.toJson(calcs, CalculationDetails.class);
	}
	
	@GetMapping("details/boxandwhisker")
	public String boxAndWhisker() {
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		BoxAndWhiskerData res=fJob.getBoxAndWhiskerData(fJob.getMinority());
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		res.setCurrent(current.getBoxAndWhiskerArray(fJob.getMinority()));
		Gson g=new Gson();
		return g.toJson(res, BoxAndWhiskerData.class);
	}
	
	@GetMapping("details/minorityDetail")
	public String minorityDetail() {
		FilteredJob fJob=(FilteredJob) getSession().getAttribute("FilteredJob");
		Districting current=(Districting) getSession().getAttribute("currentDistricting");
		DeviationEnactedDetails res=current.getMinorityDetail(fJob.getMinority());
		Gson g=new Gson();
		return g.toJson(res, DeviationEnactedDetails.class);
	}
}