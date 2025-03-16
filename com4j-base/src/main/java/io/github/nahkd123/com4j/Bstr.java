package io.github.nahkd123.com4j;

import java.lang.foreign.MemorySegment;

public interface Bstr {
	String get(MemorySegment bstr);

	void free(MemorySegment bstr);

	MemorySegment allocate(String content);

	default String getAndFree(MemorySegment bstr) {
		String content = get(bstr);
		free(bstr);
		return content;
	}
}
