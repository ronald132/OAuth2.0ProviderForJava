/*
 * Copyright 2010 Yutaka Obuchi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oauth.v2;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.net.URLDecoder;

import junit.framework.TestCase;

/**
 * @author Yutaka Obuchi
 */
public class OAuth2ValidatorTest extends TestCase {

    private long currentTimeMsec;
    private SimpleOAuth2Validator validator;
    private static final Map<String, String> PROBLEM_TO_ERROR_CODE = OAuth2.Problems.TO_ERROR_CODE;


    @Override
    protected void setUp() throws Exception {
        currentTimeMsec = (System.currentTimeMillis() / 1000) * 1000;
        validator = new SimpleOAuth2Validator() {
            @Override
            protected long currentTimeMsec() {return currentTimeMsec;}
        };
    }
    
    public void testInvalidClientIdWithPassword() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/cb","s6BhdRkqt3","gX1fBat3bV");
    	OAuth2Accessor o2a = new OAuth2Accessor(o2c);
        
    	String parameters = "grant_type=authorization_code&client_id=invalid&client_secret=gX1fBat3bV&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateClientIdWithPassword(msg,o2a);
        } catch (OAuth2ProblemException expected) {
            assertEquals(OAuth2.Problems.CLIENT_ID_MISMATCH, expected.getProblem());
        	assertEquals("invalid_client", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.CLIENT_ID_MISMATCH));
        }
    }
    public void testClientIdWithInvalidPassword() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/cb","s6BhdRkqt3","gX1fBat3bV");
    	OAuth2Accessor o2a = new OAuth2Accessor(o2c);
        
        String parameters = "grant_type=authorization_code&client_id=s6BhdRkqt3&client_secret=invalid&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateClientIdWithPassword(msg,o2a);
        } catch (OAuth2ProblemException expected) {
        	assertEquals(OAuth2.ErrorCode.INVALID_CLIENT, expected.getProblem());
        	assertEquals("unauthorized_client", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.CLIENT_SECRET_MISMATCH));
        }
    }
    public void testNoClientId() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/cb","s6BhdRkqt3","gX1fBat3bV");
    	OAuth2Accessor o2a = new OAuth2Accessor(o2c);
        
    	String parameters = "grant_type=authorization_code&client_secret=gX1fBat3bV&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateClientIdWithPassword(msg,o2a);
        } catch (OAuth2ProblemException expected) {
        	assertEquals(OAuth2.Problems.PARAMETER_ABSENT, expected.getProblem());
        	assertEquals("invalid_request", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.PARAMETER_ABSENT));
        	assertEquals("client_id", (String) expected.getParameters().get("parameter_name"));
        }
    }
    
    public void testClientIdWithNoPassword() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/cb","s6BhdRkqt3","gX1fBat3bV");
    	OAuth2Accessor o2a = new OAuth2Accessor(o2c);
        
    	String parameters = "grant_type=authorization_code&client_id=s6BhdRkqt3&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateClientIdWithPassword(msg,o2a);
        } catch (OAuth2ProblemException expected) {
        	assertEquals(OAuth2.Problems.PARAMETER_ABSENT, expected.getProblem());
        	assertEquals("invalid_request", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.PARAMETER_ABSENT));
        	assertEquals("client_secret", (String) expected.getParameters().get("parameter_name"));
        }
    }

    public void testNoRedirectUri() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/cb","s6BhdRkqt3","gX1fBat3bV");
    	String parameters = "grant_type=authorization_code&client_id=s6BhdRkqt3&client_secret=gX1fBat3bV&code=i1WsRn1uB1";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateRedirectUri(msg,o2c);
        } catch (OAuth2ProblemException expected) {
            assertEquals(OAuth2.Problems.PARAMETER_ABSENT, expected.getProblem());
        	assertEquals("invalid_request", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.PARAMETER_ABSENT));
        	assertEquals("redirect_uri", (String) expected.getParameters().get("parameter_name"));
        }
    }
    
    public void testInvalidRedirectUri() throws Exception {
    	OAuth2Client o2c = new OAuth2Client("https://client.example.com/case/invalid","s6BhdRkqt3","gX1fBat3bV");
    	String parameters = "grant_type=authorization_code&client_id=s6BhdRkqt3&client_secret=gX1fBat3bV&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateRedirectUri(msg,o2c);
        } catch (OAuth2ProblemException expected) {
        	assertEquals(OAuth2.ErrorCode.INVALID_REQUEST, expected.getProblem());
        	//assertEquals("redirect_uri_mismatch", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.REDIRECT_URI_MISMATCH));
        }
    }


    public void testInvalidScope() throws Exception {
        OAuth2Client o2c = new OAuth2Client("https://client.example.com/case/invalid","s6BhdRkqt3","gX1fBat3bV");
        String parameters = "grant_type=authorization_code&client_id=s6BhdRkqt3&scope=invalid&code=i1WsRn1uB1&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateScope(msg,o2c);
        } catch (OAuth2ProblemException expected) {
            assertEquals(OAuth2.ErrorCode.INVALID_SCOPE, expected.getProblem());
            //assertEquals("redirect_uri_mismatch", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.REDIRECT_URI_MISMATCH));
        }
    }

    public void testNoResponseType() throws Exception {
    	String parameters = "client_id=s6BhdRkqt3&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateResponseType(msg);
        } catch (OAuth2ProblemException expected) {
            //assertEquals(OAuth2.Problems.PARAMETER_ABSENT, expected.getProblem());
        	//assertEquals("invalid_request", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.PARAMETER_ABSENT));
        	//assertEquals("response_type", (String) expected.getParameters().get("parameter_name"));
            assertEquals("invalid_request", expected.getProblem());
        }
    }
    
    public void testInvalidResponseType() throws Exception {
    	String parameters = "response_type=token&client_id=s6BhdRkqt3&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb";
        OAuth2Message msg = new OAuth2Message("", "", decodeForm(parameters));
        try {
            validator.validateResponseType(msg);
        } catch (OAuth2ProblemException expected) {
            assertEquals(OAuth2.Problems.UNSUPPORTED_RESPONSE_TYPE, expected.getProblem());
        	assertEquals("unsupported_response_type", PROBLEM_TO_ERROR_CODE.get(OAuth2.Problems.UNSUPPORTED_RESPONSE_TYPE));
        }
    }

    
    public static List<OAuth2.Parameter> decodeForm(String form) {
        List<OAuth2.Parameter> list = new ArrayList<OAuth2.Parameter>();
        if (!isEmpty(form)) {
            for (String nvp : form.split("\\&")) {
                int equals = nvp.indexOf('=');
                String name;
                String value;
                if (equals < 0) {
                    name = decodePercent(nvp);
                    value = null;
                } else {
                    name = decodePercent(nvp.substring(0, equals));
                    value = decodePercent(nvp.substring(equals + 1));
                }
                list.add(new OAuth2.Parameter(name, value));
            }
        }
        return list;
    }

    public static String decodePercent(String s) {
    	try {
    		return URLDecoder.decode(s, "UTF-8");
    		// This implements http://oauth.pbwiki.com/FlexibleDecoding
    	} catch (java.io.UnsupportedEncodingException wow) {
    		throw new RuntimeException(wow.getMessage(), wow);
    	}
    }

    public static boolean isEmpty(String str) {
    	return (str == null) || (str.length() == 0);
    }
}
