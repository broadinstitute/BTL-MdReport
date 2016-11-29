/**
  * Created by Amr on 10/21/2016.
  */
import org.broadinstitute.mdreport.RetrieveMetrics
import org.scalatest.{FlatSpec, Matchers}

class RetrieveMetricsSpec extends FlatSpec with Matchers {

  "RetrieveMetrics" should "return a list of expected metrics with non-default versioning" in {
    val metrics = RetrieveMetrics.retrieve("EntryCreatorSystemTest1", Some(1L), true)
    val expected = List(",ErccStats,,,,,,PicardMeanQualByCycle,,PicardReadGcMetrics," +
      "RnaSeqQcStats,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,PicardAlignmentSummaryAnalysis" +
      ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,PicardInsertSizeMetrics" +
      ",,,,,,,,,,,,,,,,,",
      ",fractionErccReads,fractionGenomeReferenceReads,totalReads,totalUnalignedReads,fractionUnalignedReads," +
        "totalErccReads,r2MeanQual,r1MeanQual,meanGcContent,readMetrics,,,,,,,,,,,covMetrics,," +
        "alignmentMetrics,,,,,,,,,,gapMetrics,,,annotationMetrics,,,,,,,endMetrics,,,,,,,,,,note,sample," +
        "PicardAlignmentSummaryMetrics,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,," +
        "widthOf70Pct,widthOf40Pct,meanInsertSize,widthOf10Pct,widthOf30Pct,widthOf50Pct,widthOf90Pct," +
        "readPairs,widthOf99Pct,maxInsertSize,medianAbsoluteDeviation,pairOrientation,widthOf20Pct," +
        "medianInsertSize,minInsertSize,standardDeviation,widthOf80Pct,widthOf60Pct", ",,,,,,,,,,ReadMetrics," +
        ",,,,,,,,,,CovMetrics,,AlignmentMetrics,,,,,,,,,,GapMetrics,,,AnnotationMetrics,,,,,,,EndMetrics," +
        ",,,,,,,,,,,pfHqAlignedBases,totalReads,pfAlignedBases,pfNoiseReads,pctChimeras,pfMismatchRate," +
        "pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate,pctPfReadsAligned,pfHqAlignedReads," +
        "readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned,pfReads,category,pfHqMedianMismatches," +
        "badCycles,meanReadLength,strandBalance,pfIndelRate,pfHqAlignedBases,totalReads,pfAlignedBases," +
        "pfNoiseReads,pctChimeras,pfMismatchRate,pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate," +
        "pctPfReadsAligned,pfHqAlignedReads,readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned," +
        "pfReads,category,pfHqMedianMismatches,badCycles,meanReadLength,strandBalance,pfIndelRate," +
        "pfHqAlignedBases,totalReads,pfAlignedBases,pfNoiseReads,pctChimeras,pfMismatchRate," +
        "pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate,pctPfReadsAligned,pfHqAlignedReads," +
        "readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned,pfReads,category,pfHqMedianMismatches," +
        "badCycles,meanReadLength,strandBalance,pfIndelRate,,,,,,,,,,,,,,,,,,",
      ",,,,,,,,,,fragmentLengthMean,chimericPairs,readLength,estimatedLibrarySize,fragmentLengthStdDev," +
          "expressionProfilingEfficiency,unpairedReads,baseMismatchRate,transcriptsDetected," +
          "totalPurityFilteredReadsSequenced,failedVendorQCCheck,meanPerBaseCov,meanCV,mapped,mappedPairs," +
          "alternativeAlignments,uniqueRateofMapped,mappingRate,mappedUnique,end1MappingRate,end2MappingRate," +
          "mappedUniqueRateofTotal,duplicationRateOfMapped,cumulGapLength,gapPct,numGaps,intronicRate,rRNA," +
          "genesDetected,rRNArate,exonicRate,intragenicRate,intergenicRate,end1PctSense,end1Antisense," +
          "noCovered5Prime,end2MismatchRate,end2Antisense,end2PctSense,end2Sense,end1MismatchRate,end1Sense," +
          "fivePrimeNorm,,,,,,,,,,,,,,,,,,,,,,,,,1,,,,,,,,,,,,,,,,,,,,,,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,",
      ".EntryCreatorSystemTest1,0.0319489,0.84419,2368092,293314,0.123861,75658" +
        ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,," +
        ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,",
      "test.EntryCreatorSystemTest1,0.0319489,0.84419,2368092,293314,0.123861,75658,34.99,36.18,48.0,144,21916," +
        "25,2907488,-1.0,0.643255,0,0.0022569934,35367,2368092,0,0.45236537,1.6897788,2074778,1037349,-1," +
        "0.8410529,0.8761391,1744998,0.8761391,0.8761391,0.7368793,0.15894713,1350146,0.6802336,17621,0.15844394," +
        "0,9913,-1.0,0.7341928,0.8926367,0.10732715,48.7648,398990,-1,379270.0,396519,48.888294,379270,379752.0," +
        "379752,0.33317196,Notes,Mouse-A2-single,0,1184046,0,0,0.0,0.0,-1,2.8E-5,1.0,0.0,0.0,0,0,0.0,0,1184046," +
        "FIRST_OF_PAIR,0.0,0,25,0,0.0,0,1184046,0,0,0.0,0.0,-1,7.9E-5,1.0,0.0,0.0,0,0,0.0,0,1184046," +
        "SECOND_OF_PAIR,0.0,0,25,0,0.0,0,2368092,0,0,0.0,0.0,-1,5.4E-5,1.0,0.0,0.0,0,0,0.0,0,2368092," +
        "PAIR,0.0,0,25,0,0.0,1317,393,610.388274,105,299,499,12077,872499,102991,1071533,249,FR,205,481,17," +
        "592.897433,4577,649")
    metrics should contain theSameElementsAs expected
  }
  it should "return a list of expected metrics with default versioning" in {
    val metrics = RetrieveMetrics.retrieve("SSF-1859", Some(1477612237045L), true)
    val expected = List (",ErccStats,,,,,,PicardInsertSizeMetrics,,,,,,,,,,,,,,,,,,PicardMeanQualByCycle,,PicardAlignmentSummaryAnalysis,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,RnaSeqQcStats,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,",
      ",fractionErccReads,fractionGenomeReferenceReads,totalReads,totalUnalignedReads,fractionUnalignedReads,totalErccReads,widthOf70Pct,widthOf40Pct,meanInsertSize,widthOf10Pct,widthOf30Pct,widthOf50Pct,widthOf90Pct,readPairs,widthOf99Pct,maxInsertSize,medianAbsoluteDeviation,pairOrientation,widthOf20Pct,medianInsertSize,minInsertSize,standardDeviation,widthOf80Pct,widthOf60Pct,r2MeanQual,r1MeanQual,PicardAlignmentSummaryMetrics,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,readMetrics,,,,,,,,,,,covMetrics,,alignmentMetrics,,,,,,,,,,gapMetrics,,,annotationMetrics,,,,,,,endMetrics,,,,,,,,,,note,sample",
      ",,,,,,,,,,,,,,,,,,,,,,,,,,,pfHqAlignedBases,totalReads,pfAlignedBases,pfNoiseReads,pctChimeras,pfMismatchRate,pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate,pctPfReadsAligned,pfHqAlignedReads,readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned,pfReads,category,pfHqMedianMismatches,badCycles,meanReadLength,strandBalance,pfIndelRate,pfHqAlignedBases,totalReads,pfAlignedBases,pfNoiseReads,pctChimeras,pfMismatchRate,pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate,pctPfReadsAligned,pfHqAlignedReads,readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned,pfReads,category,pfHqMedianMismatches,badCycles,meanReadLength,strandBalance,pfIndelRate,pfHqAlignedBases,totalReads,pfAlignedBases,pfNoiseReads,pctChimeras,pfMismatchRate,pfHqAlignedQ20Bases,pctAdapter,pctPfReads,pfHqErrorRate,pctPfReadsAligned,pfHqAlignedReads,readsAlignedInPairs,pctReadsAlignedInPairs,pfReadsAligned,pfReads,category,pfHqMedianMismatches,badCycles,meanReadLength,strandBalance,pfIndelRate,ReadMetrics,,,,,,,,,,,CovMetrics,,AlignmentMetrics,,,,,,,,,,GapMetrics,,,AnnotationMetrics,,,,,,,EndMetrics,,,,,,,,,,,",
      ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,1,,,,,,,,,,,,,,,,,,,,,,2,,,,,,,,,,,,,,,,,,,,,,fragmentLengthMean,chimericPairs,readLength,estimatedLibrarySize,fragmentLengthStdDev,expressionProfilingEfficiency,unpairedReads,baseMismatchRate,transcriptsDetected,totalPurityFilteredReadsSequenced,failedVendorQCCheck,meanPerBaseCov,meanCV,mapped,mappedPairs,alternativeAlignments,uniqueRateofMapped,mappingRate,mappedUnique,end1MappingRate,end2MappingRate,mappedUniqueRateofTotal,duplicationRateOfMapped,cumulGapLength,gapPct,numGaps,intronicRate,rRNA,genesDetected,rRNArate,exonicRate,intragenicRate,intergenicRate,end1PctSense,end1Antisense,noCovered5Prime,end2MismatchRate,end2Antisense,end2PctSense,end2Sense,end1MismatchRate,end1Sense,fivePrimeNorm,,",
      "SSF-1859.SSF1859A05_A375_AkiYoda,-1.0,-1.0,-1,-1,-1.0,-1,1633,401,530.242512,107,305,499,27863,239639,1625323,1854929,249,FR,207,442,14,547.146537,6589,625,32.9,33.94,0,4040389,0,0,0.0,0.0,-1,1.8E-5,1.0,0.0,0.0,0,0,0.0,0,4040389,FIRST_OF_PAIR,0.0,0,25,0,0.0,0,4040389,0,0,0.0,0.0,-1,3.6E-5,1.0,0.0,0.0,0,0,0.0,0,4040389,SECOND_OF_PAIR,0.0,0,25,0,0.0,0,8080778,0,0,0.0,0.0,-1,2.7E-5,1.0,0.0,0.0,0,0,0.0,0,8080778,PAIR,0.0,0,25,0,0.0,95,217794,25,262185,-1.0,0.0643391,0,0.015464121,31732,8080778,0,0.14290248,3.1504533,1286542,569574,-1,0.37253195,0.15921016,479278,0.15921016,0.15921016,0.059310872,0.62746805,1232963,0.8934171,6071,0.05929227,0,6725,-1.0,0.40411428,0.46340656,0.42215878,60.560226,108810,-1,125430.0,132709,48.5901,125430,167079.0,167079,0.33349794,Notes,SSF1859A05_A375_AkiYoda",
      "SSF-1859.SSF1859A04_A375_AkiYoda,-1.0,-1.0,-1,-1,-1.0,-1,1041,383,513.236825,99,291,481,28341,404941,1615725,1933543,240,FR,197,445,12,505.19057,5891,603,32.81,33.88,0,8577661,0,0,0.0,0.0,-1,2.3E-5,1.0,0.0,0.0,0,0,0.0,0,8577661,FIRST_OF_PAIR,0.0,0,25,0,0.0,0,8577661,0,0,0.0,0.0,-1,5.5E-5,1.0,0.0,0.0,0,0,0.0,0,8577661,SECOND_OF_PAIR,0.0,0,25,0,0.0,0,17155322,0,0,0.0,0.0,-1,3.9E-5,1.0,0.0,0.0,0,0,0.0,0,17155322,PAIR,0.0,0,25,0,0.0,86,639418,25,408068,-1.0,0.060251214,0,0.010986334,38299,17155322,0,0.28525937,2.5538325,3975794,1539249,-1,0.20370321,0.2317528,809882,0.2317528,0.2317528,0.04720879,0.7962968,1118912,0.826504,8021,0.0991412,0,8192,-1.0,0.25998053,0.35912174,0.41571143,63.476162,245602,-1,294234.0,318668,48.006695,294234,426841.0,426841,0.308858,Notes,SSF1859A04_A375_AkiYoda"
      )
    metrics should contain theSameElementsAs expected
  }
}
