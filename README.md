# Semantic-Parsing-using-Joshua
Using the open-source statistical machine translation decoder Joshua on semantic parsing for the FUNQL of Geoquery

Make use of the open-source statistical machine translation decoder Joshua on
sematic parsing for the FUNQL of Geoquery. It illustrates how mapping natural language to
formal language using Joshua.


The semantic parsing in this paper refers the task that mapping natural language to formal
language as Figure 1 shows.

What is the biggest city in Kansas ?    
answer(largest(city(loc_2(stateid('kansas')))))   
Figure 1 : natural language and formal language in FUNQL    


FUNQL is a functional, variable-free query language for Geoquery domain (Kate et al., 2005).
Sematic parsing here is cast as a machine translation task which translates one natural language
to another natural language.
Hiero refers to Hierarchical phrase-based ( David Chiang.2007.), it is a SCFG with a single
nonterminal category, shows in Figure 2.


[X] ||| Arkansas ||| 'arkansas' ||| 0 0 1 0.01832 0 0 ||| 0-0   
[X] ||| How big is [X,1] ? ||| size( stateid( [X,1] ||| 0.68535 7.21837 1 0.01832 0 0.33647 ||| 0-1   
1-0 2-1 4-1   
Figure 2 : hiero grammar extracted using thrax in joshua    


A translation is correct if we can get the same answer from the GEOQUERY database as the
reference query.
Here in the evaluation module in WASP, it uses a Prolog development system SICStus to retrieve
the answer from GEOQUERY database.

Hireo:    
Got 126 correct translations of 280 test sentences    
Precision **45%**  
Phrase-based in Moses:    
Got 33 correct translations of 280 test sentences   
Precision 11.8%   
thrax-phrase:   
Got 41 correct translations of 280 test sentences   
Precision 14.6%   

See the [report](./project-report2.pdf) for more detail.

Hireo results see [this pdf](./sentence-by-Sentence Comparison.pdf)
