package io.github.nahkd123.com4j.win32;

import java.io.Serial;

public class Win32Exception extends RuntimeException {
	@Serial
	private static final long serialVersionUID = 5922604044218335642L;
	private HResult hresult;

	public Win32Exception(HResult hresult) {
		super(hresult.getMessage() != null ? hresult.getMessage() : hresult.toString());
		this.hresult = hresult;
	}

	public HResult getHResult() { return hresult; }
}
