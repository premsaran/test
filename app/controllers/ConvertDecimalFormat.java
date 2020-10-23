package controllers;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

import play.mvc.Controller;

public class ConvertDecimalFormat extends Controller{

	public static String convertDecimalFormat(Double value,String exponent){
		DecimalFormat decimalFormat = null;
		
			DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
			decimalFormatSymbols.setGroupingSeparator((",").charAt(0));
			decimalFormatSymbols.setDecimalSeparator((".").charAt(0));
			
		    decimalFormat = new DecimalFormat();
		    decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
		    decimalFormat.setGroupingSize(Integer.parseInt("2"));
		    decimalFormat.setMinimumFractionDigits(Integer.parseInt(exponent));
		    decimalFormat.setMaximumFractionDigits(Integer.parseInt(exponent));
//	if(sessionInfo.getDigitalGrouping().equals(Play.application().configuration().getString("afforde.application.reports.grouping.indianFormat.value"))){
		 return getIndianRupeeFormate(decimalFormat.format(value), ".", ",");   
//	}else
//		return decimalFormat.format(value);
	}
	
	public static String getIndianRupeeFormate(String value,String decimalSeperator,String currencySeperator){
		
		String[] splitDecimalValue = value.split("\\"+decimalSeperator);
		String withoutDecimal = splitDecimalValue[0];
		String[] splitThousandValue = withoutDecimal.split("\\"+currencySeperator);
		String data = "";
		for(String sr : splitThousandValue){
			data = data + sr;
		}
		char[] splitdata = data.toCharArray();
		String appendValue="";
		int x=0;
		int doubDec=0;
		String minusSymbol="";
		if(splitdata[0]=='-'){
			minusSymbol="-";
			splitdata = Arrays.copyOfRange(splitdata,1,splitdata.length);
		}
		if(splitdata.length >3){
			for(int i = splitdata.length; i > 0; i--){
				appendValue =appendValue+ splitdata[i-1];
				x++;
				if(x==3){
					appendValue=appendValue+currencySeperator;
				}
				else if(x>3){
					doubDec++;
					if(doubDec==2){
						if(splitdata.length!=(x)){
							appendValue =appendValue+currencySeperator;
							doubDec = 0;
						}
					}
				}
			}
			char[] finStr=appendValue.toCharArray();
			String result="";
			for (int i = finStr.length; i > 0; i--)
			{
				result=result+finStr[i-1];
			}
			if(splitDecimalValue.length<=1){
				return minusSymbol+result;
			}
			else
				return minusSymbol+result+decimalSeperator+splitDecimalValue[1];
		}else{
			if(splitDecimalValue.length>1){
				return data+decimalSeperator+splitDecimalValue[1];
			}
			else
				return data;
		}
	 }
}
