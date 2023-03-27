import java.util.ArrayList;

public class Post 
{
	private static int DUMB1_REMOVE_LIMIT = 5;
	private static double SMART1_REMOVE_LIMIT = 5;
	private static double SMART2_REMOVE_LIMIT = 5;
	private static double SMART1_SCALAR = 4.0/3.0;
	private static double SMART2_SCALAR = 4.0/3.0;
	
	private int opinionType;
	private double opinion;
	
	private int numReports;
	private double smart1Reports;
	//smart2Reports is calculated at the time of iteration
	private int numViews;
	
	private ArrayList<User> reportList;
	private ArrayList<User> smart2Reporters;
	
	private boolean malicious;
	
	private boolean dumb1Removed;
	private boolean dumb2Removed;
	private boolean smart1Removed;
	private boolean smart2Removed;
	
	private int smart2ReportListLoc;
	
	public Post(int initOpinionType, double initOpinion, boolean isMalicious)
	{
		opinionType = initOpinionType;
		opinion = initOpinion;
		numReports = 0;
		smart1Reports = 0;
		numViews = 0;
		reportList = new ArrayList<User>();
		smart2Reporters = new ArrayList<User>();
		malicious = isMalicious;
		
		dumb1Removed = false;
		dumb2Removed = false;
		smart1Removed = false;
		smart2Removed = false;
		
		smart2ReportListLoc = 0;
	}
	
	public void report(User user)
	{
		numReports++;
		smart1Reports += user.getSmart1Vote();
		reportList.add(user);
		
		//removal 'dumb' algorithm 1: remove upon reaching x (5) amount of reports
		if(numReports >= DUMB1_REMOVE_LIMIT)
		{
			dumb1Remove();
		}
		if(smart1Reports >= SMART1_REMOVE_LIMIT && !smart1Removed)
		{
			smart1Remove();
		}
		if(!smart2Removed)
		{
			smart2Reporters.add(user);
		}
	}
	public void view()
	{
		numViews++;
	}
	
	//DUMB
	public void dumb1Remove()
	{
		dumb1Removed = true;
	}
	public void dumb2Remove()
	{
		dumb2Removed = true;
	}
	
	//SMART 1
	public void smart1Remove()
	{
		smart1Removed = true;
		for(int i = 0; i < reportList.size(); i++)
		{
			reportList.get(i).smart1ScalarIncrease(SMART1_SCALAR);
		}
	}
	
	//SMART 2
	public void smart2Remove()
	{
		smart2Removed = true;
		for(int i = 0; i < smart2ReportListLoc; i++) //refund any taken points
		{
			reportList.get(i).smart2ScalarChange(SMART2_SCALAR, true); 
		}
		for(int i = 0; i < reportList.size(); i++) //award more points for successfully taking down a post
		{
			reportList.get(i).smart2ScalarChange(SMART2_SCALAR, true);
		}
	}
	public void smart2DontRemove()
	{
		int currentLoc = smart2ReportListLoc; //start where you left off iteratively
		while(currentLoc < reportList.size())
		{
			reportList.get(currentLoc).smart2ScalarChange(SMART2_SCALAR, false);
			
			currentLoc++;
		}
		smart2ReportListLoc = currentLoc; //save the location increase
	}
	public double smart2ReportScore()
	{
		double baseScore = 0;
		for(int i = 0; i < smart2Reporters.size(); i++)
		{
			baseScore += smart2Reporters.get(i).getSmart2Vote();
		}
		return baseScore;
	}
	
	public String toCSVString()
	{
		//opinion type, opinion, malicious, num views, num reports, smart 1 report value, smart 2 report value, report percentages, removals
		String csvString = opinionType + "," + opinion + "," + malicious + "," + 
							numViews + "," + numReports + "," + smart1Reports + "," + smart2ReportScore() + "," + 
							getReportPercentage() + "," + getSmart2ReportPercentage() + "," + 
							dumb1Removed + "," + dumb2Removed + "," + smart1Removed + "," + smart2Removed + "\n";
		return csvString;
	}
	
	//setters
	public void setNumReports(int newNumReports)
	{
		numReports = newNumReports;
	}
	public void setNumViews(int newNumViews)
	{
		numViews = newNumViews;
	}
	public void setSmart2Removed(boolean newSmart2Removed)
	{
		smart2Removed = newSmart2Removed;
	}
	
	//getters
	public int getOpinionType()
	{
		return opinionType;
	}
	public double getOpinion()
	{
		return opinion;
	}
	public int getNumReports()
	{
		return numReports;
	}
	public double getSmart1Reports()
	{
		return smart1Reports;
	}
	public int getNumViews()
	{
		return numViews;
	}
	public boolean isMalicious()
	{
		return malicious;
	}
	public double getReportPercentage()
	{
		double reportPercentage = ((double)numReports)/((double)numViews);
		return reportPercentage;
	}
	public double getSmart2ReportPercentage()
	{
		double reportPercentage = smart2ReportScore()/((double)numViews);
		return reportPercentage;
	}
	public ArrayList<User> getReportList()
	{
		return reportList;
	}
	
	public boolean getDumb1Removed()
	{
		return dumb1Removed;
	}
	public boolean getDumb2Removed()
	{
		return dumb2Removed;
	}
	public boolean getSmart1Removed()
	{
		return smart1Removed;
	}
	public boolean getSmart2Removed()
	{
		return smart2Removed;
	}
	
	//abstract (written afterwards)
	//introduction
	//methods - or iterations/revision over time
	//results
	//discussion (what the results mean more broadly, bigger picture)
	//conclusion
	//citations
}
