package net.codejava.model;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GraphData {

	List<String> keys;
	List<Long> count;
	List<Double> values;

	public GraphData(String key, Long count) {
		this.keys.add(key);
		this.count.add(count);
	}

	public GraphData(List<String> keys, List<Long> count) {
		super();
		this.keys = keys;
		this.count = count;
	}
	
	public GraphData(List<String> keys, List<Double> values, List<Long> count) {
		super();
		this.keys = keys;
		this.values = values;
	}
}
