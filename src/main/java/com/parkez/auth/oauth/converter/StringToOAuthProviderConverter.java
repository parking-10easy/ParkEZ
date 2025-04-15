package com.parkez.auth.oauth.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.parkez.auth.oauth.enums.OAuthProvider;


@Component
public class StringToOAuthProviderConverter implements Converter<String, OAuthProvider> {

	@Override
	public OAuthProvider convert(String source) {
		return OAuthProvider.from(source);
	}
}
