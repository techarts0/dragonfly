package cn.techarts.xkit.web;

import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.ArrayList;
import java.util.HashMap;

public class PseduQL {
	public List<String> lex(String pql) {
		var chars = pql.toCharArray();
		int length = chars.length, bgn = 0;
		var tokens = new ArrayList<String>();
		for(int i = 0; i < length; i++) {
			var ch = chars[i];
			if(ch == ',') {
				sub(tokens, pql, bgn, i);
				bgn = i + 1;
			}else if(ch == '{') {
				sub(tokens, pql, bgn, i);
				tokens.add("{");
				bgn = i + 1;
			}else if(ch == '}') {
				sub(tokens, pql, bgn, i);
				tokens.add("}");
				bgn = i + 1;
			}else if(ch == ' ') {
				if(bgn == i) bgn += 1;
			}
		}
		return tokens;
	}
	
	public Map<String, Object> syntax(List<String> tokens){
		String last = null;
		var stack = new Stack<String>();
		int lastBrace = 0, len = tokens.size();
		for(int i = 0; i < tokens.size(); i++) {
			var token = tokens.get(i);
			switch(token) {
				case "{":
					stack.push(token);
					break;
				case "}":
					
					break;
				default:
					last = token;
			}
		}
		return null;
	}
	
	private Map<String, Object> consume(Stack<String> stack, int n){
		var result = new HashMap<String, Object>();
		for(int i = 0; i < n; i++) {
			var field = stack.pop();
			if(i == n - 1) break;
			result.put(field, null);
		}
		return result;
	}
	
	private void sub(List<String> result, String src, int bgn, int end) {
		if(bgn >= end) return;
		var tmp = src.substring(bgn, end);
		if(tmp != null) result.add(tmp);
	}
	
	public static void main(String[] args) {
		//var result = lex("  { user { name, friends { name } } }   ");
		//result.forEach(token->System.out.println(token));
	}
}


