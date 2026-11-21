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

//        String scopes = String.join(" ",
//                "internal_user_mgt_create",
//                "internal_user_mgt_view",
//                "internal_user_mgt_list",
//                "internal_user_mgt_update",
//                "internal_user_mgt_delete",
//                "internal_org_user_mgt_create",
//                "internal_org_user_mgt_view",
//                "internal_org_user_mgt_list",
//                "internal_org_user_mgt_update",
//                "internal_org_user_mgt_delete",
//                "internal_organization_create",
//                "internal_organization_view",
//                "internal_organization_update",
//                "internal_organization_delete"
//        );

        String scopes = String.join(" ",
                "internal_user_mgt_create", "internal_user_mgt_view", "internal_user_mgt_list", "internal_user_mgt_update", "internal_user_mgt_delete",
                "internal_group_mgt_create", "internal_group_mgt_view", "internal_group_mgt_update", "internal_group_mgt_delete",
                "internal_org_user_mgt_create", "internal_org_user_mgt_view", "internal_org_user_mgt_list", "internal_org_user_mgt_update", "internal_org_user_mgt_delete",
                "internal_org_group_mgt_create", "internal_org_group_mgt_view", "internal_org_group_mgt_update", "internal_org_group_mgt_delete",
                "internal_org_role_mgt_create", "internal_org_role_mgt_view", "internal_org_role_mgt_update", "internal_org_role_mgt_delete",
                "internal_org_role_mgt_groups_update", "internal_org_role_mgt_permissions_update", "internal_org_role_mgt_users_update",
                "internal_organization_create", "internal_organization_view", "internal_organization_update", "internal_organization_delete",
                "internal_api_resource_create", "internal_api_resource_view", "internal_api_resource_update", "internal_api_resource_delete", "internal_org_api_resource_view"
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

//        String orgScopes = "internal_org_user_mgt_create internal_org_user_mgt_view internal_org_user_mgt_list " +
//                "internal_org_user_mgt_update internal_org_user_mgt_delete";

        String orgScopes = String.join(" ",
                "internal_org_user_mgt_create", "internal_org_user_mgt_view", "internal_org_user_mgt_list", "internal_org_user_mgt_update", "internal_org_user_mgt_delete",
                "internal_org_group_mgt_create", "internal_org_group_mgt_view", "internal_org_group_mgt_update", "internal_org_group_mgt_delete",
                "internal_org_role_mgt_create", "internal_org_role_mgt_view", "internal_org_role_mgt_update", "internal_org_role_mgt_delete",
                "internal_org_role_mgt_groups_update", "internal_org_role_mgt_permissions_update", "internal_org_role_mgt_users_update"
        );

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

    /**
     * JobDesignation එක සඳහා WSO2 Sub-Organization තුළ SCIM2 Group එකක් සෑදීම
     */
    public String createWso2Group(String designationTitle, String orgId) {
        String token = getOrganizationToken(orgId);
        String formattedGroupName = designationTitle.trim().replaceAll("\\s+", "_");

        log.info("Creating WSO2 SCIM2 Group '{}' in Org: {}", formattedGroupName, (orgId != null ? orgId : "Root"));

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Groups"
                : baseUrl + "/scim2/Groups";

        Map<String, Object> payload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:schemas:core:2.0:Group"),
                "displayName", formattedGroupName
        );

        Map response = restClient.post()
                .uri(targetUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(Map.class);

        if (response != null && response.containsKey("id")) {
            String wso2GroupId = response.get("id").toString();
            log.info("Successfully created WSO2 Group '{}'. ID: {}", formattedGroupName, wso2GroupId);
            return wso2GroupId;
        }
        throw new RuntimeException("Failed to create WSO2 SCIM2 Group for designation: " + designationTitle);
    }

    /**
     * WSO2 Role එකක් සාදා ඊට අදාළ Permissions (Scopes) සහ Group එක Bind කිරීම (SCIM2 Roles V2)
     */
    public String createWso2RoleAndBindToGroup(String roleName, List<String> permissions, String groupId, String orgId) {
        if (permissions == null || permissions.isEmpty()) {
            log.info("No permissions provided for role {}. Skipping role creation.", roleName);
            return null;
        }

        String token = getOrganizationToken(orgId);
        String formattedRoleName = roleName.trim().replaceAll("\\s+", "_") + "_ROLE";
        log.info("Creating WSO2 Role '{}' and binding to Group ID: {} in Org: {}", formattedRoleName, groupId, (orgId != null ? orgId : "Root"));

        // නිවැරදි WSO2 IS 7.3.0 SCIM2 Roles V2 Endpoint එක
        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/v2/Roles"
                : baseUrl + "/scim2/v2/Roles";


        List<Map<String, String>> groupsPayload = List.of(
                Map.of("value", groupId)
        );


        List<Map<String, String>> permissionsPayload = permissions.stream()
                .map(permission -> Map.of("value", permission))
                .toList();

        Map<String, Object> payload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:schemas:extension:2.0:Role"),
                "displayName", formattedRoleName,
                "groups", groupsPayload,
                "permissions", permissionsPayload
        );

        try {
            Map response = restClient.post()
                    .uri(targetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("id")) {
                String roleId = response.get("id").toString();
                log.info("Successfully created WSO2 Role ID: {} and bound to Group ID: {}", roleId, groupId);
                return roleId;
            }
        } catch (Exception e) {
            log.error("Failed to create WSO2 Role and bind permissions: {}", e.getMessage());
            throw new RuntimeException("WSO2 Role creation failed", e);
        }
        return null;
    }

    /**
     * Safe Rollback: DB දෝෂයකදී WSO2 Group එක මකා දැමීම
     */
    public void deleteWso2GroupSafely(String wso2GroupId, String orgId) {
        if (wso2GroupId == null || wso2GroupId.isBlank()) {
            return;
        }
        try {
            String token = getOrganizationToken(orgId);
            String targetUrl = (orgId != null && !orgId.isBlank())
                    ? baseUrl + "/o/scim2/Groups/" + wso2GroupId
                    : baseUrl + "/scim2/Groups/" + wso2GroupId;

            restClient.delete()
                    .uri(targetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully rolled back WSO2 Group ID: {}", wso2GroupId);
        } catch (Exception rollbackEx) {
            log.error("CRITICAL: Failed to rollback WSO2 Group ID: {} in Org: {}. Manual cleanup required!",
                    wso2GroupId, orgId, rollbackEx);
        }
    }

    /**
     * සේවකයෙකු (User) අදාළ Job Designation Group එකට ඇතුළත් කිරීම (SCIM2 PATCH)
     */
    public void addUserToWso2Group(String wso2GroupId, String wso2UserId, String empCode, String orgId) {
        if (wso2GroupId == null || wso2GroupId.isBlank() || wso2UserId == null || wso2UserId.isBlank()) {
            log.warn("Skipping WSO2 Group assignment: groupId or userId is null");
            return;
        }

        String token = getOrganizationToken(orgId);
        log.info("Adding WSO2 user '{}' ({}) to Group ID: {} in Org: {}", empCode, wso2UserId, wso2GroupId, orgId);

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Groups/" + wso2GroupId
                : baseUrl + "/scim2/Groups/" + wso2GroupId;

        Map<String, Object> patchPayload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
                "Operations", List.of(
                        Map.of(
                                "op", "add",
                                "path", "members",
                                "value", List.of(
                                        Map.of(
                                                "display", empCode,
                                                "value", wso2UserId
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

        log.info("Successfully added user '{}' to WSO2 Group ID: {}", empCode, wso2GroupId);
    }

    /**
     * WSO2 හි ERP API Resource එකට අලුත් Scope එකක් (Permission එකක්) ඇතුළත් කිරීම
     */

    public void registerScopeInWso2(String scopeCode, String displayName, String description) {
        String token = getAccessToken();
        String apiIdentifier = "https://api.topschicken.com/erp";

        log.info("Registering scope '{}' to WSO2 API Resource...", scopeCode);

        try {
            String searchUrl = UriComponentsBuilder.fromUriString(baseUrl + "/api/server/v1/api-resources")
                    .queryParam("filter", "identifier eq " + apiIdentifier)
                    .build()
                    .toUriString();

            Map searchResponse = restClient.get()
                    .uri(searchUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .body(Map.class);

            List resources = null;
            if (searchResponse != null && searchResponse.containsKey("apiResources")) {
                resources = (List) searchResponse.get("apiResources");
            }

            Map<String, Object> newScope = Map.of(
                    "name", scopeCode,
                    "displayName", displayName,
                    "description", description != null ? description : displayName
            );

            if (resources == null || resources.isEmpty()) {
                Map<String, Object> createPayload = Map.of(
                        "name", "Tops Chicken ERP API",
                        "identifier", apiIdentifier,
                        "description", "API Resource for Tops Chicken ERP",
                        "requiresAuthorization", true,
                        "scopes", List.of(newScope)
                );
                restClient.post()
                        .uri(baseUrl + "/api/server/v1/api-resources")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(createPayload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("WSO2 API Resource created with initial scope '{}'", scopeCode);
            } else {
                String resourceId = ((Map<?, ?>) resources.getFirst()).get("id").toString();
                Map<String, Object> patchPayload = Map.of(
                        "addedScopes", List.of(newScope)
                );
                restClient.patch()
                        .uri(baseUrl + "/api/server/v1/api-resources/" + resourceId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(patchPayload)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Added new scope '{}' to existing WSO2 API Resource ID: {}", scopeCode, resourceId);
            }
        } catch (Exception e) {
            log.error("Failed to sync scope '{}' with WSO2: {}", scopeCode, e.getMessage());
            throw new RuntimeException("Failed to register permission in WSO2 Identity Server", e);
        }
    }

    /**
     * WSO2 Role එකක Permissions (Scopes) Update කිරීම (SCIM2 Roles V2 PATCH)
     */
    public void updateWso2RolePermissions(String wso2RoleId, List<String> permissions, String orgId) {
        if (wso2RoleId == null || wso2RoleId.isBlank()) {
            log.warn("Cannot update WSO2 Role permissions: wso2RoleId is null");
            return;
        }

        String token = getOrganizationToken(orgId);
        log.info("Updating permissions for WSO2 Role ID: {} in Org: {}", wso2RoleId, (orgId != null ? orgId : "Root"));

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/v2/Roles/" + wso2RoleId
                : baseUrl + "/scim2/v2/Roles/" + wso2RoleId;

        List<Map<String, String>> permissionsPayload = (permissions != null)
                ? permissions.stream().map(p -> Map.of("value", p)).toList()
                : List.of();

        Map<String, Object> patchPayload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
                "Operations", List.of(
                        Map.of(
                                "op", "replace",
                                "path", "permissions",
                                "value", permissionsPayload
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

        log.info("Successfully updated permissions for WSO2 Role ID: {}", wso2RoleId);
    }

    /**
     * Safe Rollback: DB දෝෂයකදී WSO2 Role එක මකා දැමීම
     */
    public void deleteWso2RoleSafely(String wso2RoleId, String orgId) {
        if (wso2RoleId == null || wso2RoleId.isBlank()) {
            return;
        }
        try {
            String token = getOrganizationToken(orgId);
            String targetUrl = (orgId != null && !orgId.isBlank())
                    ? baseUrl + "/o/scim2/v2/Roles/" + wso2RoleId
                    : baseUrl + "/scim2/v2/Roles/" + wso2RoleId;

            restClient.delete()
                    .uri(targetUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully rolled back WSO2 Role ID: {}", wso2RoleId);
        } catch (Exception rollbackEx) {
            log.error("CRITICAL: Failed to rollback WSO2 Role ID: {} in Org: {}. Manual cleanup required!",
                    wso2RoleId, orgId, rollbackEx);
        }
    }

    /**
     * සේවකයෙකු (User) පරණ Job Designation Group එකෙන් ඉවත් කිරීම (SCIM2 PATCH)
     */
    public void removeUserFromWso2Group(String wso2GroupId, String wso2UserId, String orgId) {
        if (wso2GroupId == null || wso2GroupId.isBlank() || wso2UserId == null || wso2UserId.isBlank()) {
            log.warn("Skipping WSO2 Group removal: groupId or userId is null");
            return;
        }

        String token = getOrganizationToken(orgId);
        log.info("Removing WSO2 user '{}' from Group ID: {} in Org: {}", wso2UserId, wso2GroupId, orgId);

        String targetUrl = (orgId != null && !orgId.isBlank())
                ? baseUrl + "/o/scim2/Groups/" + wso2GroupId
                : baseUrl + "/scim2/Groups/" + wso2GroupId;

        Map<String, Object> patchPayload = Map.of(
                "schemas", List.of("urn:ietf:params:scim:api:messages:2.0:PatchOp"),
                "Operations", List.of(
                        Map.of(
                                "op", "remove",
                                "path", "members[value eq \"" + wso2UserId + "\"]"
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

        log.info("Successfully removed user '{}' from WSO2 Group ID: {}", wso2UserId, wso2GroupId);
    }

    /**
     * Safe Rollback: DB දෝෂයකදී WSO2 Sub-Organization එක මකා දැමීම
     */
    public void deleteWso2OrganizationSafely(String wso2OrgId) {
        if (wso2OrgId == null || wso2OrgId.isBlank()) {
            return;
        }
        try {
            String token = getAccessToken();
            restClient.delete()
                    .uri(baseUrl + "/api/server/v1/organizations/" + wso2OrgId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully rolled back WSO2 Organization ID: {}", wso2OrgId);
        } catch (Exception rollbackEx) {
            log.error("CRITICAL: Failed to rollback WSO2 Organization ID: {}. Manual cleanup required!",
                    wso2OrgId, rollbackEx);
        }
    }
}