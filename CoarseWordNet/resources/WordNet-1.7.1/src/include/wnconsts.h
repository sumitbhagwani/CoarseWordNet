/*

   wnconsts.h - constants used by all parts of WordNet system

   $Id: wnconsts.h,v 1.71 2002/03/22 20:19:53 wn Exp $

*/

#ifndef _WNCONSTS_
#define _WNCONSTS_

#define WN1_7
#define WN1_7_1

/* Platform specific path and filename specifications */

#ifdef UNIX
#define DICTDIR         "/dict"
#define DEFAULTPATH	"/usr/local/WordNet-1.7.1/dict"
#define DEFAULTBIN      "/usr/local/WordNet-1.7.1/bin"
#define DATAFILE	"%s/data.%s"
#define INDEXFILE	"%s/index.%s"
#define SENSEIDXFILE	"%s/index.sense"
#define KEYIDXFILE	"%s/index.key"
#define REVKEYIDXFILE	"%s/index.key.rev"
#ifdef WN1_6
#define COUSINFILE	"%s/cousin.tops"
#define COUSINEXCFILE	"%s/cousin.exc"
#endif
#define VRBSENTFILE     "%s/sents.vrb"
#define VRBIDXFILE	"%s/sentidx.vrb"
#define CNTLISTFILE     "%s/cntlist.rev"
#endif
#ifdef PC
#define DICTDIR         "\\dict"
#define DEFAULTPATH	"c:\\WordNet 1.7.1\\dict"
#define DEFAULTBIN      "c:\\WordNet 1.7.1\\bin"
#define DATAFILE	"%s\\%s.dat"
#define INDEXFILE	"%s\\%s.idx"
#define SENSEIDXFILE	"%s\\sense.idx"
#define KEYIDXFILE	"%s\\key.idx"
#define REVKEYIDXFILE	"%s\\revkey.idx"
#ifdef WN1_6
#define COUSINFILE	"%s\\cousin.tps"
#define COUSINEXCFILE	"%s\\cousin.exc"
#endif
#define VRBSENTFILE  	"%s\\sents.vrb"
#define VRBIDXFILE	"%s\\sentidx.vrb"
#define CNTLISTFILE     "%s\\cntlist.rev"
#endif
#ifdef MAC
#define DICTDIR         ":Database"
#define DEFAULTPATH     ":Database"
#define DEFAULTBIN      ":"
#define DATAFILE	"%s:data.%s"
#define INDEXFILE	"%s:index.%s"
#define SENSEIDXFILE	"%s:index.sense"
#define KEYIDXFILE	"%s:index.key"
#ifdef WN1_6
#define COUSINFILE	"%s:cousin.tops"
#define COUSINEXCFILE	"%s:cousin.exc"
#endif
#define VRBSENTFILE     "%s:sents.vrb"
#define VRBIDXFILE 	"%s:sentidx.vrb"
#define CNTLISTFILE     "%s:cntlist.rev"
#endif

/* Various buffer sizes */

#define SEARCHBUF	((long)(200*(long)1024))
#define LINEBUF		(15*1024) /* 15K buffer to read index & data files */
#define SMLINEBUF	(3*1024) /* small buffer for output lines */
#define WORDBUF		(256)	/* buffer for one word or collocation */

#define ALLSENSES	0	/* pass to findtheinfo() if want all senses */
#define MAXID		15	/* maximum id number in lexicographer file */
#define MAXDEPTH	20	/* maximum tree depth - used to find cycles */
#define MAXSENSE	75	/* maximum number of senses in database */
#define MAX_FORMS	5	/* max # of different 'forms' word can have */
#define MAXFNUM		44	/* maximum number of lexicographer files */

/* Pointer type and search type counts */

/* Pointers */

#define ANTPTR           1	/* ! */
#define HYPERPTR         2	/* @ */
#define HYPOPTR          3	/* ~ */
#define ENTAILPTR        4	/* * */
#define SIMPTR           5	/* & */

#define ISMEMBERPTR      6	/* #m */
#define ISSTUFFPTR       7	/* #s */
#define ISPARTPTR        8	/* #p */

#define HASMEMBERPTR     9	/* %m */
#define HASSTUFFPTR     10	/* %s */
#define HASPARTPTR      11	/* %p */

#define MERONYM         12	/* % (not valid in lexicographer file) */
#define HOLONYM         13	/* # (not valid in lexicographer file) */
#define CAUSETO         14	/* > */
#define PPLPTR	        15	/* < */
#define SEEALSOPTR	16	/* ^ */
#define PERTPTR		17	/* \ */
#define ATTRIBUTE	18	/* = */
#define VERBGROUP	19	/* $ */
#define NOMINALIZATIONS 20	/* + */
#define CLASSIFICATION  21	/* ; */
#define CLASS           22	/* - */

#define LASTTYPE	CLASS

/* Misc searches */

#define SYNS            (LASTTYPE + 1)
#define FREQ            (LASTTYPE + 2)
#define FRAMES          (LASTTYPE + 3)
#define COORDS          (LASTTYPE + 4)
#define RELATIVES	(LASTTYPE + 5)
#define HMERONYM        (LASTTYPE + 6)
#define HHOLONYM	(LASTTYPE + 7)
#define WNGREP		(LASTTYPE + 8)
#define OVERVIEW	(LASTTYPE + 9)

#define MAXSEARCH       OVERVIEW

/* Specific nominalization pointers */

#define NOMIN_START     (OVERVIEW + 1)

#define NOMIN_V_ATE     (NOMIN_START)          /* +a */
#define NOMIN_V_IFY     (NOMIN_START + 1)      /* +b */
#define NOMIN_V_ISE_IZE (NOMIN_START + 2)      /* +c */
#define NOMIN_ACY       (NOMIN_START + 3)      /* +d */
#define NOMIN_AGE       (NOMIN_START + 4)      /* +e */
#define NOMIN_AL        (NOMIN_START + 5)      /* +f */
#define NOMIN_ANCE_ENCE (NOMIN_START + 6)      /* +g */
#define NOMIN_ANCY_ENCY (NOMIN_START + 7)      /* +h */
#define NOMIN_ANT_ENT   (NOMIN_START + 8)      /* +i */
#define NOMIN_ARD       (NOMIN_START + 9)      /* +j */
#define NOMIN_ARY       (NOMIN_START + 10)     /* +k */
#define NOMIN_ATE       (NOMIN_START + 11)     /* +l */
#define NOMIN_ATION     (NOMIN_START + 12)     /* +m */
#define NOMIN_EE        (NOMIN_START + 13)     /* +n */
#define NOMIN_ER        (NOMIN_START + 14)     /* +o */
#define NOMIN_ERY_RY    (NOMIN_START + 15)     /* +p */
#define NOMIN_ING_INGS  (NOMIN_START + 16)     /* +q */
#define NOMIN_ION       (NOMIN_START + 17)     /* +r */
#define NOMIN_IST       (NOMIN_START + 18)     /* +s */
#define NOMIN_MENT      (NOMIN_START + 19)     /* +t */
#define NOMIN_OR        (NOMIN_START + 20)     /* +u */
#define NOMIN_URE       (NOMIN_START + 21)     /* +v */
#define NOMIN_MISC      (NOMIN_START + 22)     /* +w */
#define NOMIN_UNMARKED  (NOMIN_START + 23)     /* +x */

#define NOMIN_END       NOMIN_UNMARKED

#define CLASSIF_START    (NOMIN_END + 1)

#define CLASSIF_CATEGORY (CLASSIF_START)        /* ;c */
#define CLASSIF_USAGE    (CLASSIF_START + 1)    /* ;u */
#define CLASSIF_REGIONAL (CLASSIF_START + 2)    /* ;r */

#define CLASSIF_END      CLASSIF_REGIONAL

#define CLASS_START      (CLASSIF_END + 1)

#define CLASS_CATEGORY   (CLASS_START)          /* -c */
#define CLASS_USAGE      (CLASS_START + 1)      /* -u */
#define CLASS_REGIONAL   (CLASS_START + 2)      /* -r */

#define CLASS_END        CLASS_REGIONAL

#define MAXPTR          CLASS_END

/* WordNet part of speech stuff */

#define NUMPARTS	4	/* number of parts of speech */
#define NUMFRAMES	35	/* number of verb frames */

/* Generic names for part of speech */

#define NOUN		1
#define VERB		2
#define ADJ		3
#define ADV		4
#define SATELLITE	5	/* not really a part of speech */
#define ADJSAT		SATELLITE

#define ALL_POS		0	/* passed to in_wn() to check all POS */

#define bit(n) ((unsigned int)((unsigned int)1<<((unsigned int)n)))

/* Adjective markers */

#define PADJ		1	/* (p) */
#define NPADJ		2	/* (a) */
#define IPADJ		3	/* (ip) */

#define UNKNOWN_MARKER		0
#define ATTRIBUTIVE		NPADJ
#define PREDICATIVE		PADJ
#define IMMED_POSTNOMINAL	IPADJ

#endif				/* _WNCONSTS_ */

