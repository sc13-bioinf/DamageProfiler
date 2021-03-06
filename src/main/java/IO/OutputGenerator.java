package IO;

import calculations.DamageProfiler;
import calculations.Frequencies;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.log4j.Logger;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYDataset;


import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;
import java.awt.geom.Rectangle2D;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by neukamm on 7/8/15.
 */
public class OutputGenerator {

    //private final List<String> chrs;
    private int numberOfUsedReads;
    private final double height;
    private final Logger LOG;
    private String outpath;
    private Frequencies frequencies;
    private DamageProfiler damageProfiler;
    private int max_length;
    private int min_length;
    private String specie;
    private int threshold;
    private int length;



    public OutputGenerator(String outputFolder, DamageProfiler damageProfiler, String specie, int threshold,
                           int length, double height, Logger LOG)
            throws IOException{

        this.outpath = outputFolder;
        this.frequencies = damageProfiler.getFrequencies();
        //this.chrs = damageProfiler.getChrs();
        this.numberOfUsedReads = damageProfiler.getNumberOfUsedReads();
        this.damageProfiler = damageProfiler;
        this.threshold = threshold;
        this.length = length;
        this.height = height;
        this.LOG = LOG;

        // set tax id if specified by user
        if(specie != null && !specie.equals("")){
            this.specie = specie;
        }

        // What was this for ??
        //if(this.length > this.chrs.size(){
        //    this.length = this.chrs.size();
        //}


    }


    /**
     * create tab-separated txt file with all read length, sorted according strand direction
     *
     * @param input
     * @throws IOException
     */
    public void writeLengthDistribution(String input) throws IOException{

        BufferedWriter lgdist = new BufferedWriter(new FileWriter(this.outpath + "/lgdistribution.txt"));

        HashMap<Integer, Integer> map_forward = damageProfiler.getLength_distribution_map_forward();
        HashMap<Integer, Integer> map_reverse = damageProfiler.getLength_distribution_map_reverse();

        lgdist.write("# table produced by calculations.DamageProfiler\n");
        lgdist.write("# using mapped file " + input + "\n");
        lgdist.write("# Std: strand of reads\n");
        lgdist.write("Std\tLength\tOccurrences\n");

        // fill file

        List<Integer> key_list = new ArrayList<>();
        key_list.addAll(map_forward.keySet());
        Collections.sort(key_list);

        if(key_list.size()>0){
            min_length = key_list.get(0);
            max_length = key_list.get(key_list.size()-1);

            for(int key : key_list){
                lgdist.write("+\t" + key + "\t" + map_forward.get(key) + "\n");
            }
        }

        key_list.clear();
        key_list.addAll(map_reverse.keySet());
        Collections.sort(key_list);

        if(key_list.size()>0){
            if(key_list.get(0) < min_length){
                min_length = key_list.get(0);
            }
            if(key_list.get(key_list.size()-1) > max_length){
                max_length = key_list.get(key_list.size()-1);

            }
            for(int key : key_list){
                lgdist.write("-\t" + key + "\t" + map_reverse.get(key) + "\n");
            }

        }


        lgdist.close();
    }


    public void writeDNAcomp_genome(Frequencies frequencies) throws IOException{

        BufferedWriter file_dna_comp = new BufferedWriter(new FileWriter(outpath + File.separator + "DNA_comp_genome.txt"));
        file_dna_comp.write("DNA base frequencies Sample\n");
        file_dna_comp.write("A\tC\tG\tT\n");
        double totalSample = frequencies.getCountAllBasesSample();
        double a_percentage_sample = frequencies.getCountA_sample() / totalSample;
        double c_percentage_sample = frequencies.getCountC_sample() / totalSample;
        double g_percentage_sample = frequencies.getCountG_sample() / totalSample;
        double t_percentage_sample = frequencies.getCountT_sample() / totalSample;

        file_dna_comp.write(a_percentage_sample + "\t" + c_percentage_sample+ "\t" + g_percentage_sample + "\t" + t_percentage_sample + "\n");


        file_dna_comp.write("\nDNA base frequencies Reference\n");
        file_dna_comp.write("A\tC\tG\tT\n");
        double totalRef = frequencies.getCountAllBasesRef();
        double a_percentage_ref = frequencies.getCountA_ref() / totalRef;
        double c_percentage_ref = frequencies.getCountC_ref() / totalRef;
        double g_percentage_ref = frequencies.getCountG_ref() / totalRef;
        double t_percentage_ref = frequencies.getCountT_ref() / totalRef;

        file_dna_comp.write(a_percentage_ref + "\t" + c_percentage_ref+ "\t" + g_percentage_ref + "\t" + t_percentage_ref + "\n");

        file_dna_comp.close();

    }


    public void writeDNAComp(Frequencies frequencies) throws IOException{
        BufferedWriter freq_file_sample = new BufferedWriter(new FileWriter(outpath + File.separator + "DNA_composition_sample.txt"));

        /*
         fill now line per line
         Scheme:

               End	Std pos	A	C	G	T	Total
               3p    +
               ....
               3p    -
               ....
               5p    +
               ....
               5p    -
               ....

          */

        // write header
        freq_file_sample.write("End\tStd\tPos\tA\tC\tG\tT\tTotal\n");

        // fill '3p +'
        for(int i = 0; i < this.length; i++){
            double sum=frequencies.getCount_total_forward_3()[i];

            freq_file_sample.write("3p\t+\t"+(i+1)+"\t"
                    +(int)frequencies.getCountA_forward_3()[i]+"\t"+(int)frequencies.getCountC_forward_3()[i]+"\t"
                    +(int)frequencies.getCountG_forward_3()[i]+"\t"+(int)frequencies.getCountT_forward_3()[i]+"\t"
                    +(int)sum+"\n"
            );

        }

        // fill '3p -'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCount_total_reverse_3()[i];

            freq_file_sample.write("3p\t-\t"+(i+1)+"\t"
                    +(int)frequencies.getCountA_reverse_3()[i]+"\t"+(int)frequencies.getCountC_reverse_3()[i]+"\t"
                    +(int)frequencies.getCountG_reverse_3()[i]+"\t"+(int)frequencies.getCountT_reverse_3()[i]+"\t"
                    +(int)sum+"\n"
            );

        }


        // fill '5p +'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCount_total_forward_5()[i];

            freq_file_sample.write("5p\t+\t"+(i+1)+"\t"
                    +(int)frequencies.getCountA_forward_5()[i]+"\t"+(int)frequencies.getCountC_forward_5()[i]+"\t"
                    +(int)frequencies.getCountG_forward_5()[i]+"\t"+(int)frequencies.getCountT_forward_5()[i]+"\t"
                    +(int)sum+"\n"
            );

        }

        // fill '5p -'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCount_total_reverse_5()[i];

            freq_file_sample.write("5p\t-\t"+(i+1)+"\t"
                    +(int)frequencies.getCountA_reverse_5()[i]+"\t"+(int)frequencies.getCountC_reverse_5()[i]+"\t"
                    +(int)frequencies.getCountG_reverse_5()[i]+"\t"+(int)frequencies.getCountT_reverse_5()[i]+"\t"
                    +(int)sum+"\n");


        }
        freq_file_sample.close();
    }

    public void writeFrequenciesReference(Frequencies frequencies) throws IOException{
        BufferedWriter freq_file_ref = new BufferedWriter(new FileWriter(outpath + File.separator + "misincorporation.txt"));

        /*
         fill now line per line
         Scheme:

               End	Std pos	A	C	G	T	Total	G>A	C>T	A>G	T>C	A>C	A>T	C>G	C>A	T>G	T>A	G>C	G>T
               3p    +
               ....
               3p    -
               ....
               5p    +
               ....
               5p    -
               ....

          */

        // write header
        freq_file_ref.write("Chr\tEnd\tStd\tPos\tA\tC\tG\tT\tTotal\tG>A\tC>T\tA>G\tT>C\tA>C\tA>T\tC>G\tC>A\tT>G\tT>A\tG>C\tG>T\tA>-\tT>-\tC>-\tG>-\t->A\t->T\t->C\t->G\tS\n");

        // fill '3p +'
        for(int i = 0; i < this.length; i++){
            double sum=frequencies.getCountA_ref_forward_3()[i]+frequencies.getCountC_ref_forward_3()[i]+
                    frequencies.getCountG_ref_forward_3()[i]+frequencies.getCountT_ref_forward_3()[i];

            if(this.numberOfUsedReads>0){
                freq_file_ref.write("fwd\t3p\t+\t"+(i+1)+"\t"
                        +frequencies.getCountA_ref_forward_3()[i]+"\t"+frequencies.getCountC_ref_forward_3()[i]+"\t"
                        +frequencies.getCountG_ref_forward_3()[i]+"\t"+frequencies.getCountT_ref_forward_3()[i]+"\t"
                        +sum+"\t"
                        +frequencies.getCount_forward_G_A_3()[i]+"\t"+frequencies.getCount_forward_C_T_3()[i]+"\t"
                        +frequencies.getCount_forward_A_G_3()[i]+"\t"+frequencies.getCount_forward_T_C_3()[i]+"\t"
                        +frequencies.getCount_forward_A_C_3()[i]+"\t"+frequencies.getCount_forward_A_T_3()[i]+"\t"
                        +frequencies.getCount_forward_C_G_3()[i]+"\t"+frequencies.getCount_forward_C_A_3()[i]+"\t"
                        +frequencies.getCount_forward_T_G_3()[i]+"\t"+frequencies.getCount_forward_T_A_3()[i]+"\t"
                        +frequencies.getCount_forward_G_C_3()[i]+"\t"+frequencies.getCount_forward_G_T_3()[i]+"\t"
                        +frequencies.getCount_forward_A_0_3()[i]+"\t"+frequencies.getCount_forward_T_0_3()[i]+"\t"
                        +frequencies.getCount_forward_C_0_3()[i]+"\t"+frequencies.getCount_forward_G_0_3()[i]+"\t"
                        +frequencies.getCount_forward_0_A_3()[i]+"\t"+frequencies.getCount_forward_0_T_3()[i]+"\t"
                        +frequencies.getCount_forward_0_C_3()[i]+"\t"+frequencies.getCount_forward_0_G_3()[i]+"\t"
                        +frequencies.getCountS_forward_3()[i]+"\n"
                );
            }
        }

        // fill '3p -'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCountA_ref_reverse_3()[i]+frequencies.getCountC_ref_reverse_3()[i]+
                    frequencies.getCountG_ref_reverse_3()[i]+frequencies.getCountT_ref_reverse_3()[i];

            if(this.numberOfUsedReads>0){
                freq_file_ref.write("rev\t3p\t-\t"+(i+1)+"\t"
                        +frequencies.getCountA_ref_reverse_3()[i]+"\t"+frequencies.getCountC_ref_reverse_3()[i]+"\t"
                        +frequencies.getCountG_ref_reverse_3()[i]+"\t"+frequencies.getCountT_ref_reverse_3()[i]+"\t"
                        +sum+"\t"
                        +frequencies.getCount_reverse_G_A_3()[i]+"\t"+frequencies.getCount_reverse_C_T_3()[i]+"\t"
                        +frequencies.getCount_reverse_A_G_3()[i]+"\t"+frequencies.getCount_reverse_T_C_3()[i]+"\t"
                        +frequencies.getCount_reverse_A_C_3()[i]+"\t"+frequencies.getCount_reverse_A_T_3()[i]+"\t"
                        +frequencies.getCount_reverse_C_G_3()[i]+"\t"+frequencies.getCount_reverse_C_A_3()[i]+"\t"
                        +frequencies.getCount_reverse_T_G_3()[i]+"\t"+frequencies.getCount_reverse_T_A_3()[i]+"\t"
                        +frequencies.getCount_reverse_G_C_3()[i]+"\t"+frequencies.getCount_reverse_G_T_3()[i]
                        +frequencies.getCount_reverse_A_0_3()[i]+"\t"+frequencies.getCount_reverse_T_0_3()[i]+"\t"
                        +frequencies.getCount_reverse_C_0_3()[i]+"\t"+frequencies.getCount_reverse_G_0_3()[i]+"\t"
                        +frequencies.getCount_reverse_0_A_3()[i]+"\t"+frequencies.getCount_reverse_0_T_3()[i]+"\t"
                        +frequencies.getCount_reverse_0_C_3()[i]+"\t"+frequencies.getCount_reverse_0_G_3()[i]+"\t"
                        +frequencies.getCountS_reverse_3()[i]+"\n"
                );
            }
        }


        // fill '5p +'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCountA_ref_forward_5()[i]+frequencies.getCountC_ref_forward_5()[i]+
                    frequencies.getCountG_ref_forward_5()[i]+frequencies.getCountT_ref_forward_5()[i];

            if(this.numberOfUsedReads>0){
                freq_file_ref.write("fwd\t5p\t+\t"+(i+1)+"\t"
                        +frequencies.getCountA_ref_forward_5()[i]+"\t"+frequencies.getCountC_ref_forward_5()[i]+"\t"
                        +frequencies.getCountG_ref_forward_5()[i]+"\t"+frequencies.getCountT_ref_forward_5()[i]+"\t"
                        +sum+"\t"
                        +frequencies.getCount_forward_G_A_5()[i]+"\t"+frequencies.getCount_forward_C_T_5()[i]+"\t"
                        +frequencies.getCount_forward_A_G_5()[i]+"\t"+frequencies.getCount_forward_T_C_5()[i]+"\t"
                        +frequencies.getCount_forward_A_C_5()[i]+"\t"+frequencies.getCount_forward_A_T_5()[i]+"\t"
                        +frequencies.getCount_forward_C_G_5()[i]+"\t"+frequencies.getCount_forward_C_A_5()[i]+"\t"
                        +frequencies.getCount_forward_T_G_5()[i]+"\t"+frequencies.getCount_forward_T_A_5()[i]+"\t"
                        +frequencies.getCount_forward_G_C_5()[i]+"\t"+frequencies.getCount_forward_G_T_5()[i]+
                        +frequencies.getCount_forward_A_0_5()[i]+"\t"+frequencies.getCount_forward_T_0_5()[i]+"\t"
                        +frequencies.getCount_forward_C_0_5()[i]+"\t"+frequencies.getCount_forward_G_0_5()[i]+"\t"
                        +frequencies.getCount_forward_0_A_5()[i]+"\t"+frequencies.getCount_forward_0_T_5()[i]+"\t"
                        +frequencies.getCount_forward_0_C_5()[i]+"\t"+frequencies.getCount_forward_0_G_5()[i]+"\t"
                        +frequencies.getCountS_forward_5()[i]+"\n"

                );
            }
        }

        // fill '5p -'
        for(int i = 0; i < this.length; i++){
            double sum = frequencies.getCountA_ref_reverse_5()[i]+frequencies.getCountC_ref_reverse_5()[i]+
                    frequencies.getCountG_ref_reverse_5()[i]+frequencies.getCountT_ref_reverse_5()[i];

            if(this.numberOfUsedReads>0){
                freq_file_ref.write("rev\t5p\t-\t"+(i+1)+"\t"
                        +frequencies.getCountA_ref_reverse_5()[i]+"\t"+frequencies.getCountC_ref_reverse_5()[i]+"\t"
                        +frequencies.getCountG_ref_reverse_5()[i]+"\t"+frequencies.getCountT_ref_reverse_5()[i]+"\t"
                        +sum+"\t"
                        +frequencies.getCount_reverse_G_A_5()[i]+"\t"+frequencies.getCount_reverse_C_T_5()[i]+"\t"
                        +frequencies.getCount_reverse_A_G_5()[i]+"\t"+frequencies.getCount_reverse_T_C_5()[i]+"\t"
                        +frequencies.getCount_reverse_A_C_5()[i]+"\t"+frequencies.getCount_reverse_A_T_5()[i]+"\t"
                        +frequencies.getCount_reverse_C_G_5()[i]+"\t"+frequencies.getCount_reverse_C_A_5()[i]+"\t"
                        +frequencies.getCount_reverse_T_G_5()[i]+"\t"+frequencies.getCount_reverse_T_A_5()[i]+"\t"
                        +frequencies.getCount_reverse_G_C_5()[i]+"\t"+frequencies.getCount_reverse_G_T_5()[i]+
                        +frequencies.getCount_reverse_A_0_5()[i]+"\t"+frequencies.getCount_reverse_T_0_5()[i]+"\t"
                        +frequencies.getCount_reverse_C_0_5()[i]+"\t"+frequencies.getCount_reverse_G_0_5()[i]+"\t"
                        +frequencies.getCount_reverse_0_A_5()[i]+"\t"+frequencies.getCount_reverse_0_T_5()[i]+"\t"
                        +frequencies.getCount_reverse_0_C_5()[i]+"\t"+frequencies.getCount_reverse_0_G_5()[i]+"\t"
                        +frequencies.getCountS_reverse_5()[i]+"\n"

                );
            }
        }
        freq_file_ref.close();

    }


    /**
     * plot length distribution as histogram.
     * first histogram: distribution over all reads lengths
     * second histogram: length distribution of forward and reverse strand in one plot
     *
     * @param length_all
     * @param length_forward
     * @param length_reverse
     * @param file
     * @throws IOException
     * @throws DocumentException
     */
    public void plotLengthHistogram(List<Double> length_all, List<Double> length_forward, List<Double> length_reverse,
                                    String file) throws  IOException, DocumentException {

        Histogram hist_all = new Histogram(LOG);
        hist_all.addData(length_all);
        HistogramDataset dataset_all = hist_all.createDataset(new String[]{"all reads"}, max_length);
        JFreeChart chart_all = hist_all.createChart(dataset_all, "Read length distribution", "Read length", "Occurrences");

        Histogram hist_separated = new Histogram(LOG);
        if(length_forward.size()>0){
            hist_separated.addData(length_forward);
        }
        if(length_reverse.size()>0){
            hist_separated.addData(length_reverse);
        }
        HistogramDataset dataset_separated = hist_separated.createDataset(new String[]{"+ strand", "- strand"}, max_length);
        JFreeChart chart_separated = hist_separated.createChart(dataset_separated, "Read length distribution", "Read length", "Occurrences");

        createPdf("/Length_plot.pdf", new JFreeChart[]{chart_all, chart_separated}, file);


    }

    /**
     * plots histogram of the identity distribution of the reads
     *
     * @param distances
     * @param title
     * @param file
     * @throws DocumentException
     * @throws IOException
     */
    public void plotIdentitiyHistogram(ArrayList distances, String title, String file) throws DocumentException, IOException{
        Histogram hist_all = new Histogram(LOG);
        hist_all.addData(distances);
        HistogramDataset dataset = hist_all.createDataset(new String[]{title}, 100);
        JFreeChart chart_all = hist_all.createChart(dataset,  "Read identity distribution", "Identity", "Occurrences");
        createPdf("/identity_histogram.pdf", new JFreeChart[]{chart_all}, file);

    }

    /**
     * write percentage of misincorporations per read position
     *
     * @param gToA_reverse
     * @param cToT
     * @throws IOException
     */
    public void writeDamageFiles(double[] gToA_reverse, double[] cToT) throws IOException{

        BufferedWriter writer3Prime = new BufferedWriter(new FileWriter(this.outpath + "/3pGtoA_freq.txt"));
        BufferedWriter writer5Prime = new BufferedWriter(new FileWriter(this.outpath + "/5pCtoT_freq.txt"));

        writeDamagePattern("pos\t5pC>T\n", getSubArray(cToT, this.threshold), writer5Prime);
        writeDamagePattern("pos\t3pG>A\n", getSubArray(gToA_reverse, this.threshold), writer3Prime);

        writer3Prime.close();
        writer5Prime.close();

    }

    private void writeDamagePattern(String title, double[] values, BufferedWriter writer) throws IOException{

        writer.write(title);
        for(int i = 0; i < values.length; i++){
            Double d = values[i];
            BigDecimal bd = d == null ? BigDecimal.ZERO : BigDecimal.valueOf(d);
            if(i!=values.length-1) {
                writer.write(i+1 + "\t" + bd.toPlainString() + "\n");
            } else {
                writer.write(i+1 + "\t" + bd.toPlainString());
            }
        }

    }



    public void writeMisincorporations(Frequencies frequencies, int threshold) throws IOException {

        Locale.setDefault(Locale.US);

        BufferedWriter writer5PrimeAll = new BufferedWriter(new FileWriter(this.outpath + "/5p_freq_misincorporations.txt"));
        BufferedWriter writer3PrimeAll = new BufferedWriter(new FileWriter(this.outpath + "/3p_freq_misincorporations.txt"));


           /*

                        5 prime end
         */

        // write header
        writer5PrimeAll.write("Pos\tC>T\tG>A\tA>C\tA>G\tA>T\tC>A\tC>G\tG>C\tG>T\tT>A\tT>C\tT>G\t" +
                "->ACGT\tACGT>-\n");

        // red
        double[] C_T_5_norm = frequencies.getCount_C_T_5_norm();
        // blue
        double[] G_A_5_norm = frequencies.getCount_G_A_5_norm();

        // purple (insertions)
        double[] insertions_mean5 = getMean(frequencies.getCount_0_A_5_norm(),frequencies.getCount_0_C_5_norm(),
                frequencies.getCount_0_G_5_norm(),frequencies.getCount_0_T_5_norm());

        // green (deletions)
        // plot only mean of deletions
        double[] deletions_mean5 = getMean(frequencies.getCount_A_0_5_norm(), frequencies.getCount_C_0_5_norm() ,
                frequencies.getCount_G_0_5_norm(), frequencies.getCount_T_0_5_norm());

        // gray
        double[] A_C_5_norm = frequencies.getCount_A_C_5_norm();
        double[] A_G_5_norm = frequencies.getCount_A_G_5_norm();
        double[] A_T_5_norm = frequencies.getCount_A_T_5_norm();
        double[] C_A_5_norm = frequencies.getCount_C_A_5_norm();
        double[] C_G_5_norm = frequencies.getCount_C_G_5_norm();
        double[] G_C_5_norm = frequencies.getCount_G_C_5_norm();
        double[] G_T_5_norm = frequencies.getCount_G_T_5_norm();
        double[] T_A_5_norm = frequencies.getCount_T_A_5_norm();
        double[] T_C_5_norm = frequencies.getCount_T_C_5_norm();
        double[] T_G_5_norm = frequencies.getCount_T_G_5_norm();


        for(int pos = 0; pos < threshold; pos++){
            writer5PrimeAll.write(pos + "\t" +
                    String.format("%.6f", C_T_5_norm[pos]) + "\t" +
                    String.format("%.6f",G_A_5_norm[pos]) + "\t" +
                    String.format("%.6f",A_C_5_norm[pos]) + "\t" +
                    String.format("%.6f",A_G_5_norm[pos]) + "\t" +
                    String.format("%.6f",A_T_5_norm[pos]) + "\t" +
                    String.format("%.6f", C_A_5_norm[pos]) + "\t" +
                    String.format("%.6f",C_G_5_norm[pos]) + "\t" +
                    String.format("%.6f",G_C_5_norm[pos]) + "\t" +
                    String.format("%.6f",G_T_5_norm[pos]) + "\t" +
                    String.format("%.6f",T_A_5_norm[pos]) + "\t" +
                    String.format("%.6f",T_C_5_norm[pos]) + "\t" +
                    String.format("%.6f",T_G_5_norm[pos] )+ "\t" +
                    String.format("%.6f",insertions_mean5[pos]) + "\t" +
                    String.format("%.6f",deletions_mean5[pos])+ "\n");

        }


        /*

                        3 prime end
         */


        double[] three_A_to_C_reverse = getSubArray(frequencies.getCount_A_C_3_norm(),threshold);
        double[] three_A_to_G_reverse = getSubArray(frequencies.getCount_A_G_3_norm(),threshold);
        double[] three_A_to_T_reverse = getSubArray(frequencies.getCount_A_T_3_norm(),threshold);

        double[] three_C_to_A_reverse = getSubArray(frequencies.getCount_C_A_3_norm(), threshold);
        double[] three_C_to_G_reverse = getSubArray(frequencies.getCount_C_G_3_norm(), threshold);
        double[] three_C_to_T_reverse = getSubArray(frequencies.getCount_C_T_3_norm(), threshold);

        double[] three_G_to_A_reverse = getSubArray(frequencies.getCount_G_A_3_norm(),threshold);
        double[] three_G_to_C_reverse = getSubArray(frequencies.getCount_G_C_3_norm(),threshold);
        double[] three_G_to_T_reverse = getSubArray(frequencies.getCount_G_T_3_norm(),threshold);

        double[] three_T_to_A_reverse = getSubArray(frequencies.getCount_T_A_3_norm(),threshold);
        double[] three_T_to_C_reverse = getSubArray(frequencies.getCount_T_C_3_norm(),threshold);
        double[] three_T_to_G_reverse = getSubArray(frequencies.getCount_T_G_3_norm(),threshold);

//        three_A_to_C_reverse = reverseArray(three_A_to_C_reverse);
//        three_A_to_G_reverse = reverseArray(three_A_to_G_reverse);
//        three_A_to_T_reverse = reverseArray(three_A_to_T_reverse);
//
//        three_C_to_A_reverse = reverseArray(three_C_to_A_reverse);
//        three_C_to_G_reverse = reverseArray(three_C_to_G_reverse);
//        three_C_to_T_reverse = reverseArray(three_C_to_T_reverse);
//
//        three_G_to_A_reverse = reverseArray(three_G_to_A_reverse);
//        three_G_to_C_reverse = reverseArray(three_G_to_C_reverse);
//        three_G_to_T_reverse = reverseArray(three_G_to_T_reverse);
//
//        three_T_to_A_reverse = reverseArray(three_T_to_A_reverse);
//        three_T_to_C_reverse = reverseArray(three_T_to_C_reverse);
//        three_T_to_G_reverse = reverseArray(three_T_to_G_reverse);


        double[] insertions_mean_3 = getMean(frequencies.getCount_0_A_3_norm(),frequencies.getCount_0_C_3_norm(),
                frequencies.getCount_0_G_3_norm(),frequencies.getCount_0_T_3_norm());

        // green (deletions)
        double[] deletions_mean_3 = getMean(frequencies.getCount_A_0_3_norm(), frequencies.getCount_C_0_3_norm() ,
                frequencies.getCount_G_0_3_norm(), frequencies.getCount_T_0_3_norm());
        // write header
        writer3PrimeAll.write("Pos\tC>T\tG>A\tA>C\tA>G\tA>T\tC>A\tC>G\tG>C\tG>T\tT>A\tT>C\tT>G\t" +
                "->ACGT\tACGT>-\n");

        for(int pos = threshold-1; pos >=0; pos--){

            writer3PrimeAll.write(pos + "\t" +
                    String.format("%.6f", three_C_to_T_reverse[pos]) + "\t" +
                    String.format("%.6f", three_G_to_A_reverse[pos])+ "\t" +
                    String.format("%.6f", three_A_to_C_reverse[pos]) + "\t" +
                    String.format("%.6f", three_A_to_G_reverse[pos]) + "\t" +
                    String.format("%.6f", three_A_to_T_reverse[pos]) + "\t" +
                    String.format("%.6f", three_C_to_A_reverse[pos]) + "\t" +
                    String.format("%.6f", three_C_to_G_reverse[pos]) + "\t" +
                    String.format("%.6f", three_G_to_C_reverse[pos]) + "\t" +
                    String.format("%.6f", three_G_to_T_reverse[pos]) + "\t" +
                    String.format("%.6f", three_T_to_A_reverse[pos]) + "\t" +
                    String.format("%.6f", three_T_to_C_reverse[pos]) + "\t" +
                    String.format("%.6f", three_T_to_G_reverse[pos]) + "\t" +
                    String.format("%.6f", insertions_mean_3[pos]) + "\t" +
                    String.format("%.6f", deletions_mean_3[pos]) + "\n");

        }

        writer3PrimeAll.close();
        writer5PrimeAll.close();

    }




    /**
     * create damage plot
     *
     * @param file
     * @throws IOException
     * @throws DocumentException
     */

    public void plotMisincorporations(String file) throws IOException, DocumentException{

        double[] three_A_to_C_reverse = getSubArray(frequencies.getCount_A_C_3_norm(),threshold);
        double[] three_A_to_G_reverse = getSubArray(frequencies.getCount_A_G_3_norm(),threshold);
        double[] three_A_to_T_reverse = getSubArray(frequencies.getCount_A_T_3_norm(),threshold);

        double[] three_C_to_A_reverse = getSubArray(frequencies.getCount_C_A_3_norm(), threshold);
        double[] three_C_to_G_reverse = getSubArray(frequencies.getCount_C_G_3_norm(), threshold);
        double[] three_C_to_T_reverse = getSubArray(frequencies.getCount_C_T_3_norm(), threshold);

        double[] three_G_to_A_reverse = getSubArray(frequencies.getCount_G_A_3_norm(),threshold);
        double[] three_G_to_C_reverse = getSubArray(frequencies.getCount_G_C_3_norm(),threshold);
        double[] three_G_to_T_reverse = getSubArray(frequencies.getCount_G_T_3_norm(),threshold);

        double[] three_T_to_A_reverse = getSubArray(frequencies.getCount_T_A_3_norm(),threshold);
        double[] three_T_to_C_reverse = getSubArray(frequencies.getCount_T_C_3_norm(),threshold);
        double[] three_T_to_G_reverse = getSubArray(frequencies.getCount_T_G_3_norm(),threshold);

        three_A_to_C_reverse = reverseArray(three_A_to_C_reverse);
        three_A_to_G_reverse = reverseArray(three_A_to_G_reverse);
        three_A_to_T_reverse = reverseArray(three_A_to_T_reverse);

        three_C_to_A_reverse = reverseArray(three_C_to_A_reverse);
        three_C_to_G_reverse = reverseArray(three_C_to_G_reverse);
        three_C_to_T_reverse = reverseArray(three_C_to_T_reverse);

        three_G_to_A_reverse = reverseArray(three_G_to_A_reverse);
        three_G_to_C_reverse = reverseArray(three_G_to_C_reverse);
        three_G_to_T_reverse = reverseArray(three_G_to_T_reverse);

        three_T_to_A_reverse = reverseArray(three_T_to_A_reverse);
        three_T_to_C_reverse = reverseArray(three_T_to_C_reverse);
        three_T_to_G_reverse = reverseArray(three_T_to_G_reverse);



        // create plots
        LinePlot damagePlot_three = new LinePlot("3' end", threshold, height, LOG);
        LinePlot damagePlot_five  = new LinePlot("5' end", threshold, height, LOG);

          /*
                add data to plots

                colour code:

                red     ->  C>T
                blue    ->  G>A
                green   ->  deletions
                purple  ->  insertions
                gray    ->  all others
          */



        // five prime end
        // red
        damagePlot_five.addData(frequencies.getCount_C_T_5_norm(), "5'C>T");
        // blue
        damagePlot_five.addData(frequencies.getCount_G_A_5_norm(), "5'G>A" );

        // purple (insertions)
        double[] insertions_mean = getMean(frequencies.getCount_0_A_5_norm(),frequencies.getCount_0_C_5_norm(),
                frequencies.getCount_0_G_5_norm(),frequencies.getCount_0_T_5_norm());
        damagePlot_five.addData(insertions_mean, "insertions");

        // green (deletions)
        // plot only mean of deletions
        double[] deletions_mean = getMean(frequencies.getCount_A_0_5_norm(), frequencies.getCount_C_0_5_norm() ,
                frequencies.getCount_G_0_5_norm(), frequencies.getCount_T_0_5_norm());
        damagePlot_five.addData(deletions_mean, "deletions");

        // gray
        damagePlot_five.addData(frequencies.getCount_A_C_5_norm(), "others");
        damagePlot_five.addData(frequencies.getCount_A_G_5_norm(), "5'A>G");
        damagePlot_five.addData(frequencies.getCount_A_T_5_norm(), "5'A>T");
        damagePlot_five.addData(frequencies.getCount_C_A_5_norm(), "5'C>A");
        damagePlot_five.addData(frequencies.getCount_C_G_5_norm(), "5'C>G");
        damagePlot_five.addData(frequencies.getCount_G_C_5_norm(), "5'G>C" );
        damagePlot_five.addData(frequencies.getCount_G_T_5_norm(), "5'G>T" );
        damagePlot_five.addData(frequencies.getCount_T_A_5_norm(), "5'T>A" );
        damagePlot_five.addData(frequencies.getCount_T_C_5_norm(), "5'T>C" );
        damagePlot_five.addData(frequencies.getCount_T_G_5_norm(), "5'T>G" );

        // three prime end
        // red
        damagePlot_three.addData(three_C_to_T_reverse, "3'C>T");
        // blue
        damagePlot_three.addData(three_G_to_A_reverse, "3'G>A");

        // purple (insertions)
        double[] insertions_mean_3 = getMean(frequencies.getCount_0_A_3_norm(),frequencies.getCount_0_C_3_norm(),
                frequencies.getCount_0_G_3_norm(),frequencies.getCount_0_T_3_norm());
        damagePlot_three.addData(insertions_mean_3, "insertions");

        // green (deletions)
        double[] deletions_mean_3 = getMean(frequencies.getCount_A_0_3_norm(), frequencies.getCount_C_0_3_norm() ,
                frequencies.getCount_G_0_3_norm(), frequencies.getCount_T_0_3_norm());
        damagePlot_three.addData(deletions_mean_3, "deletions");


        // gray
        damagePlot_three.addData(three_A_to_C_reverse, "others");
        damagePlot_three.addData(three_A_to_G_reverse, "3'A>G");
        damagePlot_three.addData(three_A_to_T_reverse, "3'A>T");
        damagePlot_three.addData(three_C_to_A_reverse, "3'C>A");
        damagePlot_three.addData(three_C_to_G_reverse, "3'C>G");
        damagePlot_three.addData(three_G_to_C_reverse, "3'G>C");
        damagePlot_three.addData(three_G_to_T_reverse, "3'G>T");
        damagePlot_three.addData(three_T_to_A_reverse, "3'T>A");
        damagePlot_three.addData(three_T_to_C_reverse, "3'T>C");
        damagePlot_three.addData(three_T_to_G_reverse, "3'T>G");

        // create Dataset
        XYDataset dataset_three = damagePlot_three.createDataset();
        XYDataset dataset_five = damagePlot_five.createDataset();

        // set equal y axis range
        double ymax = Math.max(damagePlot_five.getY_max(), damagePlot_three.getY_max());

        // create damage plot five prime
        JFreeChart chart = damagePlot_five.createChart(dataset_five, ymax, threshold);
        // create damage plot three prime
        JFreeChart chart1 = damagePlot_three.createChart(dataset_three, ymax, threshold);

        createPdf("/DamagePlot.pdf", new JFreeChart[]{chart, chart1}, file);
    }


    /**
     * build reverse array
     * @param a
     * @return
     */

    private double[] reverseArray(double[] a){
        double[] a_rev = new double[a.length];
        int count = a.length;
        for(int i = 0; i < a.length; i++){
            a_rev[i] = a[count-1];
            count--;
        }

        return a_rev;

    }


    /**
     * calculates the mean per position of all input arrays
     *
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @return
     */
    private double[] getMean(double[] a1,double[] a2,double[] a3,double[] a4){
        double[] mean = new double[a1.length];
        for(int i = 0; i < a1.length; i++){
            mean[i] = (a1[i] + a2[i] + a3[i] + a4[i]);
        }
        return mean;
    }

    /**
     * returns first n elements of an array
     *
     * @param array
     * @param n
     * @return
     */
    private double[] getSubArray(double[] array, int n){

        double[] out = new double[n];

        for (int i = 0; i < n; i++){
            out[i] = array[i];
        }

        return out;

    }


    /**
     * Creates a PDF document.
     *
     * @param filename the path to the new PDF document
     * @throws DocumentException
     * @throws IOException
     *
     */
    public void createPdf(String filename, JFreeChart[] charts, String file) throws IOException, DocumentException {
        // step 1
        Document document = new Document(PageSize.A4.rotate());

        // step 2
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outpath + "/" + filename));
        // step 3
        document.open();

        // compute percentage of used reads
        double ratio_used_reads = damageProfiler.getNumberOfUsedReads() / (double) damageProfiler.getNumberOfAllReads();

        // draw text
        String title = file;
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
        DecimalFormat df = (DecimalFormat)nf;
        String read_per = Chunk.NEWLINE + "Number of used reads: " + df.format(damageProfiler.getNumberOfUsedReads()) + " (" +
                (double)(Math.round(ratio_used_reads*10000))/100 + "% of all input reads) | Specie: " + this.specie;

        Font fontbold = FontFactory.getFont("Times-Roman", 18, Font.BOLD);
        Font font = FontFactory.getFont("Times-Roman", 14);

        Paragraph para = new Paragraph();
        Chunk c_title = new Chunk(title, fontbold);
        Chunk c_underline = new Chunk(read_per, font);

        Phrase p1 = new Phrase(c_title);
        p1.add(c_underline);
        para.add(p1);

        document.add(para);

        PdfContentByte cb = writer.getDirectContent();
        float height = PageSize.A4.getWidth() * (float)0.8;
        float width = PageSize.A4.getHeight() / 2;

        // create plots, both three prime and five prime and add them
        // to one PDF

        float xpos = 0;
//        // place histogram in the center if only one plot
//        if(charts.length==1){
//            xpos=width*(float)0.5;
//        }
        for(JFreeChart chart : charts){
            PdfTemplate plot = cb.createTemplate(width, height);
            Graphics2D g2d = new PdfGraphics2D(plot, width, height);
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2d, r2d);
            g2d.dispose();
            cb.addTemplate(plot, xpos, 20);
            xpos += width;
        }

        // step 5
        document.close();
    }


}
