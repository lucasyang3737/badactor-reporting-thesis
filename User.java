import java.util.Random;

public class User 
{
	private static double ACCIDENTAL_RAND_PERCENT = 0.02;
	private static double REPORT_MALICIOUS_POST_PERCENT = 1.03989550283;
	private static double MALICIOUS_REPORT_PERCENT = 1.04602586901;
	private static int VOTE_VALUE_CAP = 3;
	
	private double op_A; //opinion on topic A
	private double op_B; //opinion on topic B
	private double op_C; //opinion on topic C
	private boolean badActor; //bad actors make up ~13% of the online population as of 2019
	// https://www.helpnetsecurity.com/2019/05/07/bad-actors-misinformation-eu-elections/ 
	private double smart1Vote;
	private double smart2Vote;
	
	public User() 
	{
		Random rand = new Random();
		op_A = rand.nextGaussian(50, 50/3);
		op_B = rand.nextGaussian(50, 50/3);
		op_C = rand.nextGaussian(50, 50/3);
		
		double actorRand = Math.random();
		if(actorRand < 0.13)
		{
			badActor = true;
		}
		else
		{
			badActor = false;
		}
		
		smart1Vote = 1;
		smart2Vote = 1;
	}
	
	public Post makePost()
	{
		double opRand = Math.random();
		int opChoice;
		
		if(opRand < (1.0/3.0))
		{
			opChoice = 1;
		}
		else if(opRand < (2.0/3.0))
		{
			opChoice = 2;
		}
		else
		{
			opChoice = 3;
		}
		
		Post post = new Post(opChoice, getOpinion(opChoice), badActor);
		return post;
	}
	public void viewPost(Post post)
	{
		post.view();
		
		if(badActor) //if they're a bad actor, use a different reporting approach
		{
			badActorViewPost(post);
			return;
		}
		
		//0.02% of reports accidental, 1/5000 seems about right (just a guess)
		double accidentalRand = Math.random()*100;
		if(accidentalRand < ACCIDENTAL_RAND_PERCENT)
		{
			post.report(this);
			return;
		}
		
		//The more someone disagrees with a post, the more likely they will report it
		//Grows exponentially starting around 1% with a rough cap at 50% if someone on one end sees a post from the other end
		//The actual reporting chance is quite low for the majority of distance, since most people do not report posts
		if(post.isMalicious())
		{
			double percentageChance = REPORT_MALICIOUS_POST_PERCENT;
			double opinion = 50;
			switch(post.getOpinionType())
			{
			case 1: 
				opinion = op_A;
				break;
			case 2:
				opinion = op_B;
				break;
			case 3:
				opinion = op_C;
				break;
			}
			double distance = Math.abs(opinion - post.getOpinion());
			
			double reportChance = Math.pow(percentageChance, distance) - 1; 
			
			double reportRand = Math.random()*100;
			if(reportRand < reportChance)
			{
				post.report(this);
			}
		}
	}
	
	private void badActorViewPost(Post post)
	{
		//The more someone disagrees with a post, the more likely they will report it
		//Grows exponentially with a rough cap at 90% if someone on one end sees a post from the other end
		
		double percentageChance = MALICIOUS_REPORT_PERCENT;
		double opinion = 50;
		switch(post.getOpinionType())
		{
		case 1: 
			opinion = op_A;
			break;
		case 2:
			opinion = op_B;
			break;
		case 3:
			opinion = op_C;
			break;
		}
		double distance = Math.abs(opinion - post.getOpinion());
		
		double reportChance = Math.pow(percentageChance, distance) - 1; 
		
		double reportRand = Math.random()*100;
		if(reportRand < reportChance)
		{
			post.report(this);
		}
	}
	
	public void smart1ScalarIncrease(double increaseScalar)
	{
		smart1Vote = smart1Vote * increaseScalar;
		if(smart1Vote > VOTE_VALUE_CAP)
		{
			smart1Vote = VOTE_VALUE_CAP;
		}
	}
	public void smart2ScalarChange(double scalar, boolean increase)
	{
		if(increase)
		{
			smart2Vote = smart2Vote * scalar;
			
			if(smart2Vote > VOTE_VALUE_CAP)
			{
				smart2Vote = VOTE_VALUE_CAP;
			}
		}
		else //decrease
		{
			smart2Vote = smart2Vote / scalar;
		}
	}
	
	//CSV String: bad actor, op A, op B, op C, smart1 Vote, smart2 Vote
	public String toCSVString()
	{
		String csvString = badActor + "," + op_A + "," + op_B + "," + op_C + "," + smart1Vote + "," + smart2Vote + "\n";
		
		return csvString;
	}
	
	//Setters
	public void setOp_A(double newOp_A)
	{
		op_A = newOp_A;
	}
	public void setOp_B(double newOp_B)
	{
		op_B = newOp_B;
	}
	public void setOp_C(double newOp_C)
	{
		op_C = newOp_C;
	}
	
	//Getters
	public double getOp_A()
	{
		return op_A;
	}
	public double getOp_B()
	{
		return op_B;
	}
	public double getOp_C()
	{
		return op_C;
	}
	public double getOpinion(int opChoice)
	{
		switch(opChoice)
		{
		case 1:
			return op_A;
		case 2:
			return op_B;
		case 3: 
			return op_C;
		}
		return -1;
	}
	public double getSmart1Vote()
	{
		return smart1Vote;
	}
	public double getSmart2Vote()
	{
		return smart2Vote;
	}
}
