import java.io.*;
import java.lang.Math;
import java.util.*;
/*
 * Maria Levandoski
 * CS 1538 Homework 2
 * Implementation of a general-purpose linear congruential generator (LCG) functio
 */
public class LCG {

  // implementation of the Chi-Square Frequency test for uniformity
  // uses ten equal sub-divisions (these are [0,0.1), [0.1,0.2), ... up to [0.9, 1.0))
  private static double chiSq(double[] gennums) {
    int freq0, freq1, freq2, freq3, freq4, freq5, freq6, freq7, freq8, freq9;
    freq0 = freq1 = freq2 = freq3 = freq4 = freq5 = freq6 = freq7 = freq8 = freq9 = 0;
    // this gets the frequencies for each subdivision (probably grossly inefficient)
    for (int i = 0; i<gennums.length; i++) {
      if (0 <= gennums[i] && gennums[i] < 0.1) freq0++;
      else if (0.1 <= gennums[i] && gennums[i] < 0.2) freq1++;
      else if (0.2 <= gennums[i] && gennums[i] < 0.3) freq2++;
      else if (0.3 <= gennums[i] && gennums[i] < 0.4) freq3++;
      else if (0.4 <= gennums[i] && gennums[i] < 0.5) freq4++;
      else if (0.5 <= gennums[i] && gennums[i] < 0.6) freq5++;
      else if (0.6 <= gennums[i] && gennums[i] < 0.7) freq6++;
      else if (0.7 <= gennums[i] && gennums[i] < 0.8) freq7++;
      else if (0.8 <= gennums[i] && gennums[i] < 0.9) freq8++;
      else if (0.9 <= gennums[i] && gennums[i] < 1) freq9++;
      else System.out.printf("Something went wrong...\n");
    }
    int[] frequencies = {freq0, freq1, freq2, freq3, freq4, freq5, freq6, freq7, freq8, freq9};


    double expecti = gennums.length/10; // expected value is total number of values divided by ten categories of equal size
    double summation = 0;
    for (int freq : frequencies) {
      double diff = freq - expecti;
      summation = summation + (diff * diff);
  	}
    double result = summation/expecti;
    return result;
  }

  // implementation of the Kolmogorov-Smirnov Test for uniformity
  // uses first 100 values from gennum only
  private static double kolmo(double[] first100) {
    double max_val, curr;
    double result = 0;
    //double[] first100 = Arrays.copyOf(gennums, 100);
    Arrays.sort(first100);
    max_val = 0;
    for (int i = 0; i < 100; i++) {
      curr = Math.max(i/100 - first100[i], first100[i] - (i-1)/100);
      if (curr > max_val) { //update max if necessary
        max_val = curr;
      }
    }
    result = max_val;
    return result;
  }

  // implementation of the Runs Test for independence
  // if value is less than expected mean of 0.50, represented by 0, else 1
  // returns z statistic
  private static double runs(double[] gennums) {
    double result = 0;
    char[] allruns = new char[gennums.length];
    int minus = 0; //minus and plus are just counts for numbers below/above the mean
    int plus = 0;
    int runs = 0; // counter for number of runs
    double mean = 0;

    for (int m = 0; m < gennums.length; m++) mean += gennums[m];
    mean = mean / gennums.length;

    for (int i = 0; i < gennums.length; i++) {
      if (gennums[i] < mean) {
        allruns[i] = 0;
        minus++;
        if (i == 0 || allruns[i-1] == 1) { //new run, increment the runs counter
          runs++;
        }
      } else {
        allruns[i] = 1;
        plus++;
        if (i == 0 || allruns[i-1] == 0) { //new run, increment the runs counter
          runs++;
        }
      }
    }
    double expectation = ((2*minus*plus)/(minus+plus)) + 1;
    double variance = (expectation-1)*(expectation-2)/(minus + plus - 1);
    double sigma = Math.sqrt(variance);
    double z_val = (runs - expectation) / sigma;
    System.out.printf("Runs test results of %d runs, %d values below the mean and %d values above the mean\n", runs, minus, plus);
    return z_val;
  }

  // implementation of the Autocorrelations test for indepedence
  // para_{X} are the various parameters for the equations
  // returns z statistic
  private static double[] autoc(double[] gennums) {
    double[] results = {0, 0, 0, 0};
    double[] z = {0,0,0,0};
    int para_N = gennums.length;
    int para_i = 0; // set arbitrarily.
    int[] para_l = {2, 3, 5, 20};
    for (int l = 0; l < 4; l++) {
      int para_M = (para_N - para_i)/para_l[l] - 1;
      for (int k = 0; k < para_M; k++) {
        results[l] += gennums[para_i+k*para_l[l]] * gennums[para_i+(k+1)*para_l[l]];
      }

      results[l] = results[l]/(para_M+1) - 0.25;
      double sigma = (Math.sqrt(13*para_M + 7))/(12*(para_M+1));
      z[l] = results[l] / sigma;
    }
    System.out.printf("Autocorrelation test results of %f, %f, %f, and %f\n", results[0], results[1], results[2], results[3]);

    return z;
  }


  // generation() handles random number generation for both standard java and LCG, depending on parameter boolean javarand
  private static double[] generation(int amount, int a, int c, double m, boolean javarand) {
    long seed = 987654321;
    double x_prev = (double)seed;
    Random rng = new Random(seed); // create a random number generator for when we are to use Java's built-in function
    double[] gennums = new double[amount]; // initialize a double array of size amount
    double num;

    try {
      FileWriter fw = new FileWriter("GeneratedNums.txt", false);
      fw.close(); // these two lines were to clear the file of any initial content
      fw = new FileWriter("GeneratedNums.txt",true);
      for (int i = 0; i < amount; i++) {
        if (javarand) {
          num = rng.nextDouble();
        } else { //this is the implementation of a general-purpose LCG
          x_prev = (a*x_prev + c) % m;
          num = x_prev / m;
        }
        fw.write(num + "\n"); // newly generated number is appended to the file with a newline
        gennums[i] = num;// also adds number to an Array for ease of use in the tests
      }
      fw.close();
      System.out.printf("Your %d randomly generated numbers were output to the file \"GeneratedNums.txt\"\n", amount);
    } catch(IOException e) {
        System.err.println("IOException: " + e.getMessage());
    }
    return gennums;
  }

  // main method, handles inputs
  public static void main(String args[]) {
    int setting = 0;
    int amount = 0;
    int test = 0;
    int a = 0;
    int c = 0;
    double m = 0;
    boolean javarand = false;
    Scanner sc = new Scanner(System.in);

    while (setting == 0) {
      System.out.printf("Please type the appropriate number to select a function setting to generate random numbers:\n");
      System.out.printf("\t1) The standard random number generator in Java\n");
      System.out.printf("\t2) LCG implementation using the initial setting a = 101427, c = 321, and m = 2^16\n");
      System.out.printf("\t3) LCG implementation using the RANDU initial setting a = 65539, c= 0, and m = 2^31\n");
      setting = sc.nextInt();
      if (setting == 1) { // Java standard random number generator
        javarand = true;
      } else if (setting == 2) {
        a = 101427;
        c = 321;
        m = Math.pow(2, 16);
      } else if (setting == 3) {
        a = 65539;
        c = 0;
        m = Math.pow(2, 31);
      } else {
        System.out.printf("Input should be either 1, 2, or 3. Please try again.\n");
        setting = 0;
      }
    }

    while (amount == 0) {
      System.out.printf("Please input an integer amount of numbers to be generated:\n");
      amount = sc.nextInt();
    }

    while (test == 0) {
      System.out.printf("Please type the appropriate number to select the statistical test to be run:\n");
      System.out.printf("\t1) Chi-Square Frequency Test for uniformity\n");
      System.out.printf("\t2) Kolmogorov-Smirnov Test for uniformity\n");
      System.out.printf("\t3) Runs Test for independence\n");
      System.out.printf("\t4) Autocorrelations test for independence\n");
      test = sc.nextInt();
    }

    // now that we have input values, generate the appropriate stream of random numbers
    double[] gennums = generation(amount, a, c, m, javarand);
    double result = 0;
    boolean option1, option2, option3;
    option1 = option2 = option3 = false;

    // runs and gets results from appropriate test
    if (test == 1) { //Chi-Square
      result = chiSq(gennums);
      System.out.printf("Chi-Square test result of %f\n", result);
      if (result < 16.92) option1 = true;
      else if (result < 14.68) option2 = true;
      else if (result < 12.2) option3 = true;
    } else if (test == 2) { //Kolmogorov-Smirnov
      result = kolmo(gennums);
      System.out.printf("Kolmogorov-Smirnov test result of %f\n", result);
      if (result > 0.136) option1 = true;
      else if (result > 0.122) option2 = true;
      else if (result > 0.107) option3 = true;
    } else if (test == 3) { //runs
      double z_stat = runs(gennums);
      System.out.printf("Runs test z result of %f", z_stat);
      if (z_stat > 1.96) option1 = true;
      else if (z_stat > 1.285) option2 = true;
      else if (z_stat > 0.845) option3 = true;
    } else { //autocorrelation
      double z[] = autoc(gennums);
      int[] l_val = {2, 3, 5, 20};
      System.out.printf("Autocorrelation z results of %f, %f, %f, and %f\n", z[0], z[1], z[2], z[3]);
      for (int i = 0; i < 4; i++) {
        if (z[i] > 1.96) option1 = true;
        else if (z[i] > 1.285) option2 = true;
        else if (z[i] > 0.845) option3 = true;
        System.out.printf("For l = %d, ", l_val[i]);
        if (option1) System.out.printf("\tWe reject the null hypothesis with 95%% confidence.\n");
        else if (option2) System.out.printf("\tWe reject the null hypothesis with 90%% confidence.\n");
        else if (option3) System.out.printf("\tWe reject the null hypothesis with 80%% confidence.\n");
        else System.out.printf("\tWe fail to reject the null hypothesis\n");
      }
    }

    if (test == 1 || test == 2 || test == 3) {
      if (option1) System.out.printf("\tWe reject the null hypothesis with 95%% confidence.\n");
      else if (option2) System.out.printf("\tWe reject the null hypothesis with 90%% confidence.\n");
      else if (option3) System.out.printf("\tWe reject the null hypothesis with 80%% confidence.\n");
      else System.out.printf("\tWe fail to reject the null hypothesis\n");
    }
  }
}
