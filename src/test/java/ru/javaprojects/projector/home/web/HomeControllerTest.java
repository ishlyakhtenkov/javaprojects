package ru.javaprojects.projector.home.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.projector.AbstractControllerTest;
import ru.javaprojects.projector.common.util.JsonUtil;
import ru.javaprojects.projector.projects.model.Tag;
import ru.javaprojects.projector.projects.to.ProjectPreviewTo;
import ru.javaprojects.projector.users.to.ProfileTo;
import ru.javaprojects.projector.users.util.UserUtil;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.projector.common.CommonTestData.*;
import static ru.javaprojects.projector.projects.ProjectTestData.*;
import static ru.javaprojects.projector.users.UserTestData.*;

class HomeControllerTest extends AbstractControllerTest {
    private static final String ABOUT_URL = "/about";
    private static final String CONTACT_URL = "/contact";
    private static final String TAGS_URL = "/tags/%s";
    private static final String SEARCH_URL = "/search";
    private static final String SEARCH_TAGS_URL = "/search/tags";

    private static final String HOME_VIEW = "home/index";
    private static final String ABOUT_VIEW = "home/about";
    private static final String CONTACT_VIEW = "home/contact";
    private static final String SEARCH_VIEW = "home/search";

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePage() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project2PreviewTo, project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project2PreviewTo, project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePageWithProjectsByAuthorWhenNotBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL)
                .param(BY_AUTHOR_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePageWithProjectsByAuthorWhenBelongs() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL)
                .param(BY_AUTHOR_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageWithProjectsByAuthorUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL)
                .param(BY_AUTHOR_PARAM, String.valueOf(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePageWithPopularProjects() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL)
                .param(POPULAR_PARAM, EMPTY_STRING))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo, project2PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePageWithPopularProjectsUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(HOME_URL)
                .param(POPULAR_PARAM, EMPTY_STRING))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project1PreviewTo, project2PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void showHomePageWithProjectsByTag() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(TAGS_URL, tag1.getName())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project2PreviewTo, project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageWithProjectsByTagUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(TAGS_URL, tag1.getName())))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_ATTRIBUTE))
                .andExpect(view().name(HOME_VIEW))
                .andExpect(result -> PROJECT_PREVIEW_TO_MATCHER
                        .assertMatchIgnoreFields((List<ProjectPreviewTo>) Objects.requireNonNull(result.getModelAndView())
                                        .getModel().get(PROJECTS_ATTRIBUTE), List.of(project2PreviewTo, project1PreviewTo),
                                "author.roles", "author.password", "author.registered"));
    }

    @Test
    void showAboutPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ABOUT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ABOUT_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAboutPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ABOUT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(ABOUT_VIEW));
    }

    @Test
    void showContactPageUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTACT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CONTACT_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showContactPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTACT_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(CONTACT_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void search() throws Exception {
        doSearch();
    }

    @Test
    void searchUnauthorized() throws Exception {
        doSearch();
    }

    @SuppressWarnings("unchecked")
    private void doSearch() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(KEYWORD_PARAM, AGGREGATOR_KEYWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(PROFILES_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(TAGS_PAGE_ATTRIBUTE))
                .andExpect(view().name(SEARCH_VIEW));
        Page<ProjectPreviewTo> projects = (Page<ProjectPreviewTo>) Objects.requireNonNull(actions.andReturn()
                        .getModelAndView()).getModel().get(PROJECTS_PAGE_ATTRIBUTE);
        assertEquals(2, projects.getTotalElements());
        assertEquals(1, projects.getTotalPages());
        Page<ProfileTo> profiles = (Page<ProfileTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROFILES_PAGE_ATTRIBUTE);
        assertEquals(0, profiles.getTotalElements());
        assertEquals(0, profiles.getTotalPages());
        Page<Tag> tags = (Page<Tag>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TAGS_PAGE_ATTRIBUTE);
        assertEquals(0, tags.getTotalElements());
        assertEquals(0, tags.getTotalPages());
        PROJECT_PREVIEW_TO_MATCHER.assertMatchIgnoreFields(projects.getContent(),
                List.of(project1PreviewTo, project2PreviewTo), "author.roles", "author.password", "author.registered");

        actions = perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(KEYWORD_PARAM, tag1.getName()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(PROFILES_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(TAGS_PAGE_ATTRIBUTE))
                .andExpect(view().name(SEARCH_VIEW));
        projects = (Page<ProjectPreviewTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECTS_PAGE_ATTRIBUTE);
        assertEquals(0, projects.getTotalElements());
        assertEquals(0, projects.getTotalPages());
        profiles = (Page<ProfileTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROFILES_PAGE_ATTRIBUTE);
        assertEquals(0, profiles.getTotalElements());
        assertEquals(0, profiles.getTotalPages());
        tags = (Page<Tag>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TAGS_PAGE_ATTRIBUTE);
        assertEquals(1, tags.getTotalElements());
        assertEquals(1, tags.getTotalPages());
        TAG_MATCHER.assertMatch(tags.getContent(), List.of(tag1));

        actions = perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(KEYWORD_PARAM, admin.getName()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PROJECTS_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(PROFILES_PAGE_ATTRIBUTE))
                .andExpect(model().attributeExists(TAGS_PAGE_ATTRIBUTE))
                .andExpect(view().name(SEARCH_VIEW));
        projects = (Page<ProjectPreviewTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROJECTS_PAGE_ATTRIBUTE);
        assertEquals(0, projects.getTotalElements());
        assertEquals(0, projects.getTotalPages());
        profiles = (Page<ProfileTo>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(PROFILES_PAGE_ATTRIBUTE);
        assertEquals(1, profiles.getTotalElements());
        assertEquals(1, profiles.getTotalPages());
        tags = (Page<Tag>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(TAGS_PAGE_ATTRIBUTE);
        assertEquals(0, tags.getTotalElements());
        assertEquals(0, tags.getTotalPages());
        PROFILE_TO_MATCHER.assertMatch(profiles.getContent(), List.of(UserUtil.asProfileTo(admin)));

    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getTagsByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(SEARCH_TAGS_URL)
                .param(KEYWORD_PARAM, tag1.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<Tag> tags = JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), Tag.class);
        TAG_MATCHER.assertMatchIgnoreFields(tags, List.of(tag1));
    }

    @Test
    void getTagsByKeywordUnauthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(SEARCH_TAGS_URL)
                .param(KEYWORD_PARAM, tag1.getName())
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        List<Tag> tags = JsonUtil.readContentFromPage(actions.andReturn().getResponse().getContentAsString(), Tag.class);
        TAG_MATCHER.assertMatchIgnoreFields(tags, List.of(tag1));
    }
}
