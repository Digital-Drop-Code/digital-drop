package net.codejava.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GraphRow {
	String key;
	int count;
	Double value;
	public GraphRow(Integer key, int count, Double counta) {
		super();
		this.key = key.toString();
		this.count = count;
		this.value = counta;
	}
	
	public GraphRow(String key, Long count, Double counta) {
		super();
		this.key = key;
		this.count = count.intValue();
		this.value = counta;
	}
	public GraphRow(String key, Long count) {
		super();
		this.key = key;
		this.count = count.intValue();
	}
}
