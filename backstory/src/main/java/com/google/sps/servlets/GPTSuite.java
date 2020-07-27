// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.IdTokenCredentials;
import com.google.auth.oauth2.IdTokenProvider;
import java.io.IOException;
import org.json.JSONObject;


public final class GPTSuite {
  /**
   * Makes a post request with a JSON including GPT2 Parameters
   */
  private static HttpResponse makePostRequestGPT2(
      String serviceUrl, String prompt, int textLength, int temperature) throws IOException {

    //Obtain Credentials
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    
    //Validate Credentials
    if (!(credentials instanceof IdTokenProvider)) {
      throw new IllegalArgumentException("Credentials are not an instance of IdTokenProvider.");
    }

    //Generate Authentication Token
    IdTokenCredentials tokenCredential = IdTokenCredentials.newBuilder()
                                             .setIdTokenProvider((IdTokenProvider) credentials)
                                             .setTargetAudience(serviceUrl)
                                             .build();

    //Formate Request Assets
    GenericUrl genericUrl = new GenericUrl(serviceUrl);
    HttpCredentialsAdapter adapter = new HttpCredentialsAdapter(tokenCredential);
    HttpTransport transport = new NetHttpTransport();
    String requestBody = "{\"length\": " + textLength
        + ",\"truncate\": \"<|endoftext|>\", \"prefix\": \"" + prompt
        + "\", \"temperature\": " + temperature + "}";
    HttpRequest request = transport.createRequestFactory(adapter).buildPostRequest(
        genericUrl, ByteArrayContent.fromString("application/json", requestBody));
    
    //Wait until response received
    request.getHeaders().setContentType("application/json");
    request.setConnectTimeout(0);
    request.setReadTimeout(0);

    return request.execute();
  }
  /**
   * Returns generated text output for a given prompt, length, and temperature.
   */
  public static String generateText(String prompt, int textLength, int temperature) {
    try {
      HttpResponse outputResponse = makePostRequestGPT2(
          "https://backstory-text-gen-pdaqhmzgva-uc.a.run.app", prompt, textLength, temperature);
      try {
        JSONObject jsonObject = new JSONObject(outputResponse.parseAsString());
        return jsonObject.getString("text");
      } catch (Exception jsonException) {
        throw new RuntimeException("Failed to convert repsonse into JSON", jsonException);
      }

    } catch (IOException serverException) {
      throw new RuntimeException("Error with server", serverException);
    }
  }
}