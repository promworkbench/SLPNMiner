package org.processmining.slpnminer.models;


public interface CrossProduct {

    Object[] getCrossProduct(ReachabilityGraph r1,
                                      State init1,
                                      AcceptStateSet fin1,
                                      ReachabilityGraph r2,
                                      State init2,
                                      AcceptStateSet fin2);

    String getProbability(ReachabilityGraph r);
}
