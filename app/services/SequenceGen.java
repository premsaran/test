package services;

import java.util.List;
import javax.persistence.TypedQuery;
import entity.NumberingPlan;
import play.db.jpa.JPA;

public class SequenceGen {
	public static String generate(String sequenceName,String tenantId) throws Exception {
		String no = null;
		TypedQuery <NumberingPlan> typedQuery=(TypedQuery<NumberingPlan>) JPA.em().createQuery("select X from NumberingPlan X where X.tenant.tenantId=:tenantId and X.sequenceName=:sequenceName",NumberingPlan.class);
		typedQuery.setParameter("tenantId",tenantId);
		typedQuery.setParameter("sequenceName",sequenceName);
		List<NumberingPlan> results = typedQuery.getResultList(); 
		for(NumberingPlan numberingPlan:results) {
         no=numberingPlan.getPrefix()+""+String.format("%05d",numberingPlan.getLastSeqNo()+numberingPlan.getIncrement())+""+numberingPlan.getSuffix();
        numberingPlan.setLastSeqNo(numberingPlan.getLastSeqNo()+numberingPlan.getIncrement());
		
	  }
		return no;
	}
}

 
