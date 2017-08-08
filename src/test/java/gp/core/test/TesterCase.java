package gp.core.test;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TesterCase {

	static ObjectMapper mapper = new ObjectMapper();
	
	public static void main(String[] args) {
		MyBean myBean = new MyBean();
		myBean.setProp1("a1");
		MyBean myBean2 = new MyBean();
		myBean2.setProp1("a11");
		myBean.setProp2(myBean2);
		Map<String, Object> fieldMap = mapper.convertValue(myBean, new TypeReference<Map<String, Object>>(){});
		
		System.out.println(fieldMap.toString());
		MyBean bean = mapper.convertValue(fieldMap, MyBean.class);
		System.out.println(bean.toString());
	}
	
	public static class MyBean{
		
		String prop1 = "";

		public String getProp1() {
			return prop1;
		}

		public void setProp1(String prop1) {
			this.prop1 = prop1;
		}
		
		MyBean prop2 = null;

		public MyBean getProp2() {
			return prop2;
		}

		public void setProp2(MyBean prop2) {
			this.prop2 = prop2;
		}

		@Override
		public String toString() {
			return "MyBean [prop1=" + prop1 + ", prop2=" + prop2 + "]";
		}
		
		
	}
}
