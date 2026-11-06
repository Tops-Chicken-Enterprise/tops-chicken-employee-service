package com.topschicken.employeeservice.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class Wso2IdentityClientService {

    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;

    private static final String ROOT_TOKEN_CACHE_KEY = "wso2:token:root";
    private static final String SUB_ORG_TOKEN_CACHE_PREFIX = "wso2:token:org:";
    private static final Duration TOKEN_TTL = Duration.ofSeconds(3000);

    @Value("${wso2.base-url}")
    private String baseUrl;

    @Value("${wso2.token-url}")
    private String tokenUrl;

    @Value("${wso2.scim-users-url}")
    private String scimUsersUrl;

    @Value("${wso2.client-id}")
    private String clientId;

    @Value("${wso2.client-secret}")
    private String clientSecret;

    @Value("${wso2.ask-password-enabled:true}")
    private boolean askPasswordEnabled;

    public Wso2IdentityClientService(
            @Qualifier("wso2RestClient") RestClient restClient,
            StringRedisTemplate redisTemplate) {
        this.restClient = restClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 1. Root Access Token ලබා ගැනීම (Redis Caching සහිතව)
     */
    private String getAccessToken() {
        try {
            String cachedToken = redisTemplate.opsForValue().get(ROOT_TOKEN_CACHE_KEY);
            if (cachedToken != null && !cachedToken.isBlank()) {
                log.debug("Using cached WSO2 Root Access Token from Redis");
                return cachedToken;
            }
        } catch (Exception e) {
            log.warn("Redis cache unavailable for Root token, fetching directly from WSO2: {}", e.getMessage());
        }

        log.info("Fetching new Root Access Token from WSO2 IS 7.3.0...");

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        String scopes = String.join(" ",
                "internal_user_mgt_create",
                "internal_user_mgt_view",
                "internal_user_mgt_list",
                "internal_user_mgt_update",
                "internal_user_mgt_delete",
                "internal_org_user_mgt_create",
                "internal_org_user_mgt_view",
                "internal_org_user_mgt_list",
                "internal_org_user_mgt_update",
                "internal_org_user_mgt_delete",
                "internal_organization_create",
                "internal_organization_view",
                "internal_organization_update",
                "internal_organization_delete"
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", scopes);

        Map response = restClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("access_token")) {
            String accessToken = response.get("access_token").toString();
            log.info("WSO2 Root Token Received and Cached!");
            cacheTokenSafely(ROOT_TOKEN_CACHE_KEY, accessToken);
            return accessToken;
        }
        throw new RuntimeException("Failed to retrieve WSO2 access token");
    }

    /**
     * 2. Sub-Organization Token ලබා ගැනීම (Redis Caching සහිතව)
     */
    private String getOrganizationToken(String targetOrgId) {
        if (targetOrgId == null || targetOrgId.isBlank()) {
            return getAccessToken();
        }

        String cacheKey = SUB_ORG_TOKEN_CACHE_PREFIX + targetOrgId;
        try {
            String cachedOrgToken = redisTemplate.opsForValue().get(cacheKey);
            if (cachedOrgToken != null && !cachedOrgToken.isBlank()) {
                log.debug("Using cached WSO2 Sub-Org Token from Redis for Org: {}", targetOrgId);
                return cachedOrgToken;
            }
        } catch (Exception e) {
            log.warn("Redis cache unavailable for Sub-Org token, switching directly via WSO2: {}", e.getMessage());
        }

        String rootToken = getAccessToken();
        log.info("Switching token to Sub-Organization ID: {}", targetOrgId);

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        String orgScopes = "internal_org_user_mgt_create internal_org_user_mgt_view internal_org_user_mgt_list " +
                "internal_org_user_mgt_update internal_org_user_mgt_delete";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "organization_switch");
        body.add("token", rootToken);
        body.add("switching_organization", targetOrgId);
        body.add("scope", orgScopes);

        Map response = restClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("access_token")) {
            String orgToken = response.get("access_token").toString();
            log.info("Successfully switched token to Sub-Org and Cached!");
            cacheTokenSafely(cacheKey, orgToken);
            return orgToken;
        }
        throw new RuntimeException("Failed to switch token for organization: " + targetOrgId);
    }

    private void cacheTokenSafely(String key, String token) {
        try {
            redisTemplate.opsForValue().set(key, token, TOKEN_TTL);
        } catch (Exception e) {
            log.warn("Failed to write token to Redis cache: {}", e.getMessage());
        }
    }

    /**
     * 3. SCIM Filter Injection වලින් ආරක්ෂිතව User සෙවීම
     */
    public String findUserByUsername(String username, String orgId) {
        String token = getOrganizationToken(orgId);

        // SCIM Injection වැළැක්වීමට quotes සහ විශේෂ ලකුණු ඉවත් කරයි
        String sanitizedUsername = username.replace("\"", "").replace("\\", "").trim();

        String baseScimUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Users"
                : scimUsersUrl;

        String targetUrl = UriComponentsBuilder.fromUriString(baseScimUrl)
                .queryParam("filter", "userName eq \"" + sanitizedUsername + "\"")
                .build()
                .toUriString();

        Map response = restClient.get()
                .uri(targetUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("totalResults")) {
            Object totalObj = response.get("totalResults");
            int total = (totalObj instanceof Integer) ? (Integer) totalObj : Integer.parseInt(totalObj.toString());
            if (total > 0) {
                List<Map<String, Object>> resources = (List<Map<String, Object>>) response.get("Resources");
                return resources.get(0).get("id").toString();
            }
        }
        return null;
    }

    /**
     * 4. Email Invitation Link (askPassword) හෝ Temporary Password මගින් User සෑදීම
     */

    /**
     * 4. Email Invitation Link (askPassword) + Secure Random Password මගින් User සෑදීම
     */
    public String createWso2User(String empCode, String email, String nic, String firstName, String lastName,
                                 String organization, String contactNumber, String orgId, String tempPassword) {
        String token = getOrganizationToken(orgId);
        log.info("Creating WSO2 user for empCode: {}, Target Org: {}, AskPassword: {}",
                empCode, (orgId != null ? orgId : "Root"), askPasswordEnabled);

        Map<String, Object> payload = new HashMap<>();
        payload.put("schemas", List.of(
                "urn:ietf:params:scim:schemas:core:2.0:User",
                "urn:scim:schemas:extension:custom:User",
                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User",
                "urn:scim:wso2:schema"
        ));
        payload.put("userName", empCode);

        // WSO2 Password Policy (min 8 chars, upper, lower, digit, special) තෘප්තිමත් කරන Secure Random Password එකක්
        String finalPassword = (tempPassword != null && !tempPassword.isBlank())
                ? tempPassword
                : generateSecureRandomPassword();
        payload.put("password", finalPassword);

        payload.put("name", Map.of(
                "givenName", firstName,
                "familyName", lastName
        ));
        payload.put("emails", List.of(Map.of("primary", true, "value", email)));

        if (contactNumber != null && !contactNumber.isBlank()) {
            payload.put("phoneNumbers", List.of(Map.of(
                    "type", "mobile",
                    "value", contactNumber
            )));
        }

        // 1. Standard Enterprise Schema (Organization නම සඳහා)
        if (organization != null && !organization.isBlank()) {
            payload.put("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", Map.of(
                    "organization", organization
            ));
        }

        // 2. WSO2 System Schema (AskPassword Email Link Flow සඳහා)
        if (askPasswordEnabled) {
            payload.put("urn:scim:wso2:schema", Map.of(
                    "askPassword", "true"
            ));
        }

        // 3. Custom Schema (NIC සඳහා)
        if (nic != null && !nic.isBlank()) {
            payload.put("urn:scim:schemas:extension:custom:User", Map.of(
                    "nic", nic
            ));
        }

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Users"
                : scimUsersUrl;

        Map response = restClient.post()
                .uri(targetUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("id")) {
            String wso2UserId = response.get("id").toString();
            log.info("Successfully created WSO2 user. ID: {}", wso2UserId);
            return wso2UserId;
        }
        throw new RuntimeException("Failed to create user in WSO2");
    }

    /**
     * WSO2 Password Policy එකට ගැලපෙන ආරක්ෂිත Random Password එකක් ජනනය කිරීම
     */
    private String generateSecureRandomPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$%&*!";
        String allChars = upper + lower + digits + special;

        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(12);

        // අනිවාර්යයෙන්ම තිබිය යුතු අක්ෂර වර්ග 4න් එක බැගින් මුලට එකතු කිරීම
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));

        // ඉතිරි අක්ෂර 8 අහඹු ලෙස තෝරා ගැනීම
        for (int i = 4; i < 12; i++) {
            sb.append(allChars.charAt(random.nextInt(allChars.length())));
        }
        return sb.toString();
    }

//    public String createWso2User(String empCode, String email, String nic, String firstName, String lastName,
//                                 String organization, String contactNumber, String orgId, String tempPassword) {
//        String token = getOrganizationToken(orgId);
//        log.info("Creating WSO2 user for empCode: {}, Target Org: {}, AskPassword: {}",
//                empCode, (orgId != null ? orgId : "Root"), askPasswordEnabled);
//
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("schemas", List.of(
//                "urn:ietf:params:scim:schemas:core:2.0:User",
//                "urn:scim:schemas:extension:custom:User",
//                "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"
//        ));
//        payload.put("userName", empCode);
//        payload.put("name", Map.of(
//                "givenName", firstName,
//                "familyName", lastName
//        ));
//        payload.put("emails", List.of(Map.of("primary", true, "value", email)));
//
//        if (contactNumber != null && !contactNumber.isBlank()) {
//            payload.put("phoneNumbers", List.of(Map.of(
//                    "type", "mobile",
//                    "value", contactNumber
//            )));
//        }
//
//        // Enterprise Schema (Organization + AskPassword Email Link Flow)
//        Map<String, Object> enterpriseExtension = new HashMap<>();
//        if (organization != null && !organization.isBlank()) {
//            enterpriseExtension.put("organization", organization);
//        }
//
//        if (askPasswordEnabled) {
//            // WSO2 මගින් සේවකයාගේ Email එකට Password Set කිරීමේ Link එක යවයි
//            enterpriseExtension.put("askPassword", "true");
//        } else {
//            payload.put("password", tempPassword);
//        }
//        payload.put("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", enterpriseExtension);
//
//        if (nic != null && !nic.isBlank()) {
//            payload.put("urn:scim:schemas:extension:custom:User", Map.of(
//                    "nic", nic
//            ));
//        }
//
//        String targetUrl = (orgId != null && !orgId.isBlank())
//                ? baseUrl + "/o/scim2/Users"
//                : scimUsersUrl;
//
//        Map response = restClient.post()
//                .uri(targetUrl)
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(payload)
//                .retrieve()
//                .body(Map.class);
//
//        if (response != null && response.containsKey("id")) {
//            String wso2UserId = response.get("id").toString();
//            log.info("Successfully created WSO2 user. ID: {}", wso2UserId);
//            return wso2UserId;
//        }
//        throw new RuntimeException("Failed to create user in WSO2");
//    }

    /**
     * 5. සේවකයාගේ තොරතුරු සහ Active/Inactive තත්ත්වය WSO2 හි Update කිරීම (SCIM PUT/PATCH)
     */
    public void updateWso2User(String wso2UserId, String email, String nic, String firstName, String lastName,
                               String organization, String contactNumber, boolean isActive, String orgId) {
        if (wso2UserId == null || wso2UserId.isBlank()) {
            log.warn("Cannot update WSO2 user: wso2UserId is null");
            return;
        }

        String token = getOrganizationToken(orgId);
        log.info("Updating WSO2 user ID: {} in Org: {}", wso2UserId, (orgId != null ? orgId : "Root"));

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Users/" + wso2UserId
                : scimUsersUrl + "/" + wso2UserId;

        Map<String, Object> patchPayload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
                "Operations", List.of(
                        Map.of(
                                "op", "replace",
                                "value", Map.of(
                                        "name", Map.of("givenName", firstName, "familyName", lastName),
                                        "emails", List.of(Map.of("primary", true, "value", email)),
                                        "phoneNumbers", List.of(Map.of("type", "mobile", "value", contactNumber != null ? contactNumber : "")),
                                        "active", isActive,
                                        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User", Map.of(
                                                "organization", organization != null ? organization : ""
                                        ),
                                        "urn:scim:schemas:extension:custom:User", Map.of(
                                                "nic", nic != null ? nic : ""
                                        )
                                )
                        )
                )
        );

        restClient.patch()
                .uri(targetUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(patchPayload)
                .retrieve()
                .toBodilessEntity();

        log.info("Successfully updated WSO2 user ID: {}", wso2UserId);
    }

    /**
     * 6. Safe Rollback - මෙහිදී Exception එකක් ආවත් මුල් DB Exception එක යටපත් (Mask) නොවේ!
     */
    public void deleteWso2UserSafely(String wso2UserId, String orgId) {
        if (wso2UserId == null || wso2UserId.isBlank()) {
            return;
        }
        try {
            String token = getOrganizationToken(orgId);
            String targetUrl = (orgId != null && !orgId.isBlank())
                    ? baseUrl + "/o/scim2/Users/" + wso2UserId
                    : scimUsersUrl + "/" + wso2UserId;

            restClient.delete()
                    .uri(targetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully rolled back (deleted) WSO2 user ID: {}", wso2UserId);
        } catch (Exception rollbackEx) {
            // මුල් Database Error එක ආරක්ෂා කර ගැනීම සඳහා මෙහිදී Error එක Log කරනවා පමණි (Throw කරන්නේ නැත)
            log.error("CRITICAL: Failed to rollback WSO2 user ID: {} in Org: {}. Manual cleanup required!",
                    wso2UserId, orgId, rollbackEx);
        }
    }

    public String createWso2Organization(String orgName, String description) {
        String token = getAccessToken();
        log.info("Creating WSO2 organization for department: {}", orgName);

        Map<String, Object> payload = Map.of(
                "name", orgName,
                "description", description != null ? description : orgName,
                "type", "TENANT"
        );

        Map response = restClient.post()
                .uri(baseUrl + "/api/server/v1/organizations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("id")) {
            String orgId = response.get("id").toString();
            log.info("Successfully created WSO2 Organization. ID: {}", orgId);
            return orgId;
        }
        throw new RuntimeException("Failed to create organization in WSO2");
    }
}