import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Main 
{
	private static int NUMUSERS = 500000; // 500k users
	private static int POSTS_PER_USER = 3;
	private static int POSTS_SEEN_PER_USER_PER_ITERATION = 200;
	private static int ITERATIONS = 5;
	private static double PERCENTILE_REMOVE_THRESHOLD = 0.014;
	private static double PERCENTILE_REMOVE_THRESHOLD_SMART2 = 0.012;
	
	private static ArrayList<User> userList = new ArrayList<User>();
	private static ArrayList<Post> postList = new ArrayList<Post>();
	
	public static void main(String[] args) 
	{
		System.out.println("Hello!");
	
		//Make Users
		makeUsers();
		
		//Making Posts
		makePosts();
		
		System.out.println("End of Phase 1");
		
		//Viewing Posts, then do iterative removal
		for(int i = 0; i < ITERATIONS; i++)
		{
			viewPosts();
			dumb2Removals();
			smart2Removals();
			System.out.println("------ END OF CYCLE " + (i + 1) + " ------");
//			System.out.print(userList.get(0).toCSVString());
//			System.out.print(postList.get(0).toCSVString());
//			writePostsToFile(i + 1);
//			writeUsersToFile(i + 1);
			System.out.println("End of Write " + (i + 1));
		}
		
		
		//calculating things
		getStatistics_static();
		getStatistics_percentile();
		getStatistics_views();
		getStatistics_smart1();
		getStatistics_smart2();
		getOverallStatistics();
	}
	
	private static void makeUsers()
	{
		for(int i = 0; i < NUMUSERS; i++)
		{
			User user = new User();
			userList.add(user);
		}
	}
	
	private static void makePosts()
	{
		Random rand = new Random();
		
		for(int i = 0; i < userList.size(); i++)
		{
			int postsMade = (int) rand.nextGaussian(POSTS_PER_USER, 1);
			
			for(int j = 0; j < postsMade; j++)
			{
				Post post = userList.get(i).makePost();
				postList.add(post);
			}
		}
	}
	
	private static void viewPosts()
	{
		Random rand = new Random();
		
		for(int i = 0; i < userList.size(); i++)
		{
			int postsSeen = (int) rand.nextGaussian(POSTS_SEEN_PER_USER_PER_ITERATION, POSTS_SEEN_PER_USER_PER_ITERATION/3);
			
//			if(i % 10000 == 0) //only for testing purposes
//			{
//				System.out.println("User " + i + " is viewing " + postsSeen + " posts");
//			}
			
			for(int j = 0; j < postsSeen; j++)
			{
				int postNum = (int) (Math.random() * postList.size());
				userList.get(i).viewPost(postList.get(postNum));
			}
		}
	}
	
	//DUMB1 MOVED TO POST, continuous
//	private static void dumb1Removals() //advantage: continuous banning 
//	{
//		for(int i = 0; i < postList.size(); i++)
//		{
//			if(postList.get(i).getNumReports() >= 5)
//			{
//				postList.get(i).dumb1Remove();
//			}
//		}
//	}
	
	private static void dumb2Removals()
	{
		// percentage-based, also provides reasoning for iterative banning rather than continuous
		for(int i = 0; i < postList.size(); i++)
		{
			if(postList.get(i).getReportPercentage() >= PERCENTILE_REMOVE_THRESHOLD)
			{
				postList.get(i).dumb2Remove();
			}
		}
	}

	// SMART1 MOVED TO POST.JAVA, continuous
//	private static void smart1Removals() //advantage: the first self-adjusting removal, continuous
//	{
//		// users who get a post that they have reported removed get their reporting value increased, no penalty for posts that are not removed
//	}
	
	private static void smart2Removals() //iterative
	{
		//users who get a post that they have reported removed get their reporting value increased, 
		//users who have reported a post that did not get removed get their reporting value decreased
		//percentage bans again
		//users who had previously had their reporting value decreased but the post gets removed in a subsequent iteration get 'refunded' their value 
		
		for(int i = 0; i < postList.size(); i++)
		{		
			if(postList.get(i).getSmart2ReportPercentage() >= PERCENTILE_REMOVE_THRESHOLD_SMART2)
			{
				postList.get(i).smart2Remove();
			}
			else
			{
				if(postList.get(i).getSmart2Removed() == true)
				{
					postList.get(i).setSmart2Removed(false);
				}
				postList.get(i).smart2DontRemove();
			}
		}
	}
	
	private static void writePostsToFile(int iteration)
	{
		try 
		{
			String filename = "iteration" + iteration + "_posts.csv";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false)); //overwrite
			
			String headerLine = "OpinionType,Opinion,Malicious,Views,Reports,Smart1Score,Smart2Score,ReportProportion,Smart2Proportion,Dumb1Removed,Dumb2Removed,Smart1Removed,Smart2Removed\n";
			writer.write(headerLine);
			
			for(int i = 0; i < postList.size(); i++)
			{
				writer.write(postList.get(i).toCSVString());
			}
			
			writer.flush();
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void writeUsersToFile(int iteration)
	{
		try 
		{
			String filename = "iteration" + iteration + "_users.csv";
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false)); //overwrite
			
			String headerLine = "BadActor,OpinionA,OpinionB,OpinionC,Smart1Value,Smart2Value\n";
			writer.write(headerLine);
			
			for(int i = 0; i < userList.size(); i++)
			{
				writer.write(userList.get(i).toCSVString());
			}
			
			writer.flush();
			writer.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void getStatistics_static()
	{
		int malishTotalReports = 0;
		int malishPosts = 0;
		int minMalishReports = Integer.MAX_VALUE;
		int maxMalishReports = Integer.MIN_VALUE;
		int nonMalishTotalReports = 0;
		int nonMalishPosts = 0;
		int minNonMalishReports = Integer.MAX_VALUE;
		int maxNonMalishReports = Integer.MIN_VALUE;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				malishPosts++;
				malishTotalReports += post.getNumReports();
				
				if(post.getNumReports() < minMalishReports)
				{
					minMalishReports = post.getNumReports();
				}
				if(post.getNumReports() > maxMalishReports)
				{
					maxMalishReports = post.getNumReports();
				}
			}
			else
			{
				nonMalishPosts++;
				nonMalishTotalReports += post.getNumReports();
				
				if(post.getNumReports() < minNonMalishReports)
				{
					minNonMalishReports = post.getNumReports();
				}
				if(post.getNumReports() > maxNonMalishReports)
				{
					maxNonMalishReports = post.getNumReports();
				}
			}
		}
		
		double avgMalishReports = (malishTotalReports + 0.0)/(malishPosts + 0.0);
		double avgNonMalishReports = (nonMalishTotalReports + 0.0)/(nonMalishPosts + 0.0);
		
		System.out.println("STATIC REPORTS\n-----------------------------------\n");
		System.out.println("Malicious Stats: \nTotal Posts = " + malishPosts + ", Average Reports = " + avgMalishReports + 
				"\nMax Reports = " + maxMalishReports + ", Min Reports = " + minMalishReports);
		System.out.println("Non-Malicious Stats: \nTotal Posts = " + nonMalishPosts + ", Average Reports = " + avgNonMalishReports + 
				"\nMax Reports = " + maxNonMalishReports + ", Min Reports = " + minNonMalishReports + "\n");
	}
	private static void getStatistics_percentile()
	{
		int malishTotalReports = 0;
		int malishTotalViews = 0;
		int malishPosts = 0;
		double minMalishReportProportion = Integer.MAX_VALUE;
		double maxMalishReportProportion = Integer.MIN_VALUE;
		
		int nonMalishTotalReports = 0;
		int nonMalishTotalViews = 0;
		int nonMalishPosts = 0;
		double minNonMalishReportProportion = Integer.MAX_VALUE;
		double maxNonMalishReportProportion = Integer.MIN_VALUE;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				malishPosts++;
				malishTotalReports += post.getNumReports();
				malishTotalViews += post.getNumViews();
				
				double reportPercentage = post.getReportPercentage();
				
				if(reportPercentage < minMalishReportProportion)
				{
					minMalishReportProportion = reportPercentage;
				}
				if(reportPercentage > maxMalishReportProportion)
				{
					maxMalishReportProportion = reportPercentage;
				}
			}
			else
			{
				nonMalishPosts++;
				nonMalishTotalReports += post.getNumReports();
				nonMalishTotalViews += post.getNumViews();
				
				double reportPercentage = post.getReportPercentage();
				
				if(reportPercentage < minNonMalishReportProportion)
				{
					minNonMalishReportProportion = reportPercentage;
				}
				if(reportPercentage > maxNonMalishReportProportion)
				{
					maxNonMalishReportProportion = reportPercentage;
				}
			}
		}
		
		double avgMalishReportPercentage = (malishTotalReports + 0.0)/(malishTotalViews + 0.0);
		double avgNonMalishReportPercentage = (nonMalishTotalReports + 0.0)/(nonMalishTotalViews + 0.0);
		
		System.out.println("PERCENTILE REPORTS\n-----------------------------------\n");
		System.out.println("Malicious Stats: \nTotal Posts = " + malishPosts + ", Average Report Proportion = " + avgMalishReportPercentage + 
				"\nMax Report Proportion = " + maxMalishReportProportion + ", Min Report Proportion = " + minMalishReportProportion);
		System.out.println("Non-Malicious Stats: \nTotal Posts = " + nonMalishPosts + ", Average Report Proportion = " + avgNonMalishReportPercentage + 
				"\nMax Report Proportion = " + maxNonMalishReportProportion + ", Min Report Proportion = " + minNonMalishReportProportion + "\n");
	}
	private static void getStatistics_views()
	{
		int malishTotalViews = 0;
		int malishPosts = 0;
		int minMalishViews = Integer.MAX_VALUE;
		int maxMalishViews = Integer.MIN_VALUE;
		int nonMalishTotalViews = 0;
		int nonMalishPosts = 0;
		int minNonMalishViews = Integer.MAX_VALUE;
		int maxNonMalishViews = Integer.MIN_VALUE;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				malishPosts++;
				malishTotalViews += post.getNumViews();
				
				if(post.getNumViews() < minMalishViews)
				{
					minMalishViews = post.getNumViews();
				}
				if(post.getNumViews() > maxMalishViews)
				{
					maxMalishViews = post.getNumViews();
				}
			}
			else
			{
				nonMalishPosts++;
				nonMalishTotalViews += post.getNumViews();
				
				if(post.getNumViews() < minNonMalishViews)
				{
					minNonMalishViews = post.getNumViews();
				}
				if(post.getNumViews() > maxNonMalishViews)
				{
					maxNonMalishViews = post.getNumViews();
				}
			}
		}
		
		double avgMalishViews = (malishTotalViews + 0.0)/(malishPosts + 0.0);
		double avgNonMalishViews = (nonMalishTotalViews + 0.0)/(nonMalishPosts + 0.0);
		
		System.out.println("VIEWS\n-----------------------------------\n");
		System.out.println("Malicious Stats: \nTotal Posts = " + malishPosts + ", Average Views = " + avgMalishViews + 
				"\nMax Views = " + maxMalishViews + ", Min Views = " + minMalishViews);
		System.out.println("Non-Malicious Stats: \nTotal Posts = " + nonMalishPosts + ", Average Views = " + avgNonMalishViews + 
				"\nMax Views = " + maxNonMalishViews + ", Min Views = " + minNonMalishViews + "\n");
	}
	private static void getStatistics_smart1()
	{
		double malishTotalSmart1Reports = 0;
		int malishPosts = 0;
		double minMalishSmart1Reports = Integer.MAX_VALUE;
		double maxMalishSmart1Reports = Integer.MIN_VALUE;
		
		double nonMalishTotalSmart1Reports = 0;
		int nonMalishPosts = 0;
		double minNonMalishSmart1Reports = Integer.MAX_VALUE;
		double maxNonMalishSmart1Reports = Integer.MIN_VALUE;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				malishPosts++;
				malishTotalSmart1Reports += post.getSmart1Reports();
				
				if(post.getSmart1Reports() < minMalishSmart1Reports)
				{
					minMalishSmart1Reports = post.getSmart1Reports();
				}
				if(post.getSmart1Reports() > maxMalishSmart1Reports)
				{
					maxMalishSmart1Reports = post.getSmart1Reports();
				}
			}
			else
			{
				nonMalishPosts++;
				nonMalishTotalSmart1Reports += post.getSmart1Reports();
				
				if(post.getSmart1Reports() < minNonMalishSmart1Reports)
				{
					minNonMalishSmart1Reports = post.getSmart1Reports();
				}
				if(post.getSmart1Reports() > maxNonMalishSmart1Reports)
				{
					maxNonMalishSmart1Reports = post.getSmart1Reports();
				}
			}
		}
		
		double avgMalishSmart1Reports = (malishTotalSmart1Reports + 0.0)/(malishPosts + 0.0);
		double avgNonMalishSmart1Reports = (nonMalishTotalSmart1Reports + 0.0)/(nonMalishPosts + 0.0);
		
		System.out.println("SMART 1 ALGORITHM REPORTS\n-----------------------------------\n");
		System.out.println("Malicious Stats: \nTotal Posts = " + malishPosts + ", Average Smart 1 Report Rating = " + avgMalishSmart1Reports + 
				"\nMax Smart 1 Report Rating = " + maxMalishSmart1Reports + ", Min Smart 1 Report Rating = " + minMalishSmart1Reports);
		System.out.println("Non-Malicious Stats: \nTotal Posts = " + nonMalishPosts + ", Average Smart 1 Report Rating = " + avgNonMalishSmart1Reports + 
				"\nMax Smart 1 Report Rating = " + maxNonMalishSmart1Reports + ", Min Smart 1 Report Rating = " + minNonMalishSmart1Reports + "\n");
	}
	private static void getStatistics_smart2()
	{
		double malishTotalSmart2Reports = 0;
		int malishPosts = 0;
		double minMalishSmart2Reports = Integer.MAX_VALUE;
		double maxMalishSmart2Reports = Integer.MIN_VALUE;
		
		double nonMalishTotalSmart2Reports = 0;
		int nonMalishPosts = 0;
		double minNonMalishSmart2Reports = Integer.MAX_VALUE;
		double maxNonMalishSmart2Reports = Integer.MIN_VALUE;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				malishPosts++;
				malishTotalSmart2Reports += post.smart2ReportScore();
				
				if(post.smart2ReportScore() < minMalishSmart2Reports)
				{
					minMalishSmart2Reports = post.smart2ReportScore();
				}
				if(post.smart2ReportScore() > maxMalishSmart2Reports)
				{
					maxMalishSmart2Reports = post.smart2ReportScore();
				}
			}
			else
			{
				nonMalishPosts++;
				nonMalishTotalSmart2Reports += post.smart2ReportScore();
				
				if(post.smart2ReportScore() < minNonMalishSmart2Reports)
				{
					minNonMalishSmart2Reports = post.smart2ReportScore();
				}
				if(post.smart2ReportScore() > maxNonMalishSmart2Reports)
				{
					maxNonMalishSmart2Reports = post.smart2ReportScore();
				}
			}
		}
		
		double avgMalishSmart2Reports = (malishTotalSmart2Reports + 0.0)/(malishPosts + 0.0);
		double avgNonMalishSmart2Reports = (nonMalishTotalSmart2Reports + 0.0)/(nonMalishPosts + 0.0);
		
		System.out.println("SMART 1 ALGORITHM REPORTS\n-----------------------------------\n");
		System.out.println("Malicious Stats: \nTotal Posts = " + malishPosts + ", Average Smart 2 Report Rating = " + avgMalishSmart2Reports + 
				"\nMax Smart 2 Report Rating = " + maxMalishSmart2Reports + ", Min Smart 2 Report Rating = " + minMalishSmart2Reports);
		System.out.println("Non-Malicious Stats: \nTotal Posts = " + nonMalishPosts + ", Average Smart 2 Report Rating = " + avgNonMalishSmart2Reports + 
				"\nMax Smart 2 Report Rating = " + maxNonMalishSmart2Reports + ", Min Smart 2 Report Rating = " + minNonMalishSmart2Reports + "\n");
	}
	private static void getOverallStatistics()
	{
		int maliciousPosts = 0;
		int dumb1MaliciousRemovals = 0;
		int dumb1MaliciousExtraViews = 0;
		int dumb2MaliciousRemovals = 0;
		int dumb2MaliciousExtraViews = 0;
		int smart1MaliciousRemovals = 0;
		int smart1MaliciousExtraViews = 0;
		int smart2MaliciousRemovals = 0;
		int smart2MaliciousExtraViews = 0;
		
		int nonMaliciousPosts = 0;
		int dumb1NonMaliciousRemovals = 0;
		int dumb2NonMaliciousRemovals = 0;
		int smart1NonMaliciousRemovals = 0;
		int smart2NonMaliciousRemovals = 0;
		
		for(int i = 0; i < postList.size(); i++)
		{
			Post post = postList.get(i);
			if(post.isMalicious())
			{
				maliciousPosts++;
				
				if(post.getDumb1Removed())
				{
					dumb1MaliciousRemovals++;
				}
				else
				{
					dumb1MaliciousExtraViews += post.getNumViews();
				}
				
				if(post.getDumb2Removed())
				{
					dumb2MaliciousRemovals++;
				}
				else
				{
					dumb2MaliciousExtraViews += post.getNumViews();
				}
				
				if(post.getSmart1Removed())
				{
					smart1MaliciousRemovals++;
				}
				else
				{
					smart1MaliciousExtraViews += post.getNumViews();
				}
				
				if(post.getSmart2Removed())
				{
					smart2MaliciousRemovals++;
				}
				else
				{
					smart2MaliciousExtraViews += post.getNumViews();
				}
			}
			else
			{
				nonMaliciousPosts++;
				
				if(post.getDumb1Removed())
				{
					dumb1NonMaliciousRemovals++;
				}
				
				if(post.getDumb2Removed())
				{
					dumb2NonMaliciousRemovals++;
				}
				
				if(post.getSmart1Removed())
				{
					smart1NonMaliciousRemovals++;
				}
				
				if(post.getSmart2Removed())
				{
					smart2NonMaliciousRemovals++;
				}
			}
		}
		
		double dumb1MaliciousRemovalProportion = ((double)dumb1MaliciousRemovals)/((double)maliciousPosts);
		double dumb2MaliciousRemovalProportion = ((double)dumb2MaliciousRemovals)/((double)maliciousPosts);
		double smart1MaliciousRemovalProportion = ((double)smart1MaliciousRemovals)/((double)maliciousPosts);
		double smart2MaliciousRemovalProportion = ((double)smart2MaliciousRemovals)/((double)maliciousPosts);
		
		double dumb1NonMaliciousRemovalProportion = ((double)dumb1NonMaliciousRemovals)/((double)nonMaliciousPosts);
		double dumb2NonMaliciousRemovalProportion = ((double)dumb2NonMaliciousRemovals)/((double)nonMaliciousPosts);
		double smart1NonMaliciousRemovalProportion = ((double)smart1NonMaliciousRemovals)/((double)nonMaliciousPosts);
		double smart2NonMaliciousRemovalProportion = ((double)smart2NonMaliciousRemovals)/((double)nonMaliciousPosts);
		
		System.out.println("OVERALL STATISTICS\n-----------------------------------\n");
		System.out.println("---- DUMB 1 STATS ----");
		System.out.println("Proportion of Malicious Posts Removed: " + dumb1MaliciousRemovalProportion);
		System.out.println("Proportion of Non-malicious Posts Removed: " + dumb1NonMaliciousRemovalProportion);
		System.out.println("No. Views of Malicious Posts that were not Removed: " + dumb1MaliciousExtraViews + "\n");
		
		System.out.println("---- DUMB 2 STATS ----");
		System.out.println("Proportion of Malicious Posts Removed: " + dumb2MaliciousRemovalProportion);
		System.out.println("Proportion of Non-malicious Posts Removed: " + dumb2NonMaliciousRemovalProportion);
		System.out.println("No. Views of Malicious Posts that were not Removed: " + dumb2MaliciousExtraViews + "\n");
		
		System.out.println("---- SMART 1 STATS ----");
		System.out.println("Proportion of Malicious Posts Removed: " + smart1MaliciousRemovalProportion);
		System.out.println("Proportion of Non-malicious Posts Removed: " + smart1NonMaliciousRemovalProportion);
		System.out.println("No. Views of Malicious Posts that were not Removed: " + smart1MaliciousExtraViews + "\n");
		
		System.out.println("---- SMART 2 STATS ----");
		System.out.println("Proportion of Malicious Posts Removed: " + smart2MaliciousRemovalProportion);
		System.out.println("Proportion of Non-malicious Posts Removed: " + smart2NonMaliciousRemovalProportion);
		System.out.println("No. Views of Malicious Posts that were not Removed: " + smart2MaliciousExtraViews + "\n");
	}
}
