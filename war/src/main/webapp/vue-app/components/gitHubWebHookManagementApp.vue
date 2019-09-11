<template>
  <v-app id="gitHubWebHookManagementApp" color="transaprent">
    <main>
      <v-layout>
        <v-flex class="white text-xs-center" flat>
          <div v-if="error && !loading" class="alert alert-error v-content">
            <i class="uiIconError"></i>
            {{ error }}
          </div>
          <div>
            <v-alert
              v-model="alert"
              dismissible
              transition="scale-transition"
              type="success"
            >{{ message }}</v-alert>
            <v-alert v-model="errorAlert" dismissible outline type="error">{{ errorMsg }}</v-alert>
            <v-toolbar flat color="white">
              <v-toolbar-title>Github webhooks</v-toolbar-title>
              <v-divider class="mx-2" inset vertical />
              <v-spacer />

              <v-spacer />

              <v-dialog v-model="dialog" max-width="500px">
                <v-btn slot="activator" color="primary" dark class="mb-2">New Connector</v-btn>
                <v-card>
                  <v-card-title>
                    <span class="headline">{{ formTitle }}</span>
                  </v-card-title>

                  <v-card-text>
                    <v-container grid-list-md>
                      <v-layout wrap>
                        <v-flex xs12 sm6 md4>
                          <v-text-field v-model="editedItem.organization" label="Organization" />
                        </v-flex>
                        <v-flex xs12 sm6 md4>
                          <v-text-field v-model="editedItem.repo" label="Repository" />
                        </v-flex>
                        <v-flex xs12 sm6 md8>
                          <v-text-field v-model="editedItem.webhook" label="WebHook" />
                        </v-flex>
                        <v-flex xs12 sm6 md4>
                          <v-checkbox v-model="editedItem.enabled" label="Enabled" />
                        </v-flex>
                      </v-layout>
                    </v-container>
                  </v-card-text>

                  <v-card-actions>
                    <v-spacer />
                    <v-btn flat @click="close">Cancel</v-btn>
                    <v-btn color="primary" @click="save">Save</v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-toolbar>
            <v-data-table 
              :headers="headers" 
              :items="hookList" 
              hide-default-footer
              class="elevation-1"
              >
              <template slot="items" slot-scope="props">
                <td class="text-xs-right">{{ props.item.organization }}</td>
                <td class="text-xs-right">{{ props.item.repo }}</td>
                <td class="text-xs-right">{{ props.item.webhook }}</td>
                <td class="text-xs-right">{{ props.item.enabled }}</td>
                <td class="justify-center layout px-0">
                  <v-icon small class="mr-2" @click="editItem(props.item)">edit</v-icon>
                  <v-icon small @click="deleteItem(props.item)">delete</v-icon>
                </td>
              </template>
              <template slot="no-data">No Webhooks</template>
            </v-data-table>
          </div>
        </v-flex>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
export default {
  data() {
    return {
      error: null,
      message: '',
      alert: false,
      errorAlert: false,
      errorMsg: '',
      dialog: false,
      hookList: [],
      headers: [
        {
          text: 'Organization',
          align: 'center',
          sortable: true,
          value: 'organization',
        },
        {
          text: 'Repository',
          align: 'center',
          sortable: true,
          value: 'repo',
        },
        {
          text: 'WebHook',
          align: 'center',
          sortable: true,
          value: 'webhook',
        },
        {
          text: 'Enabled',
          align: 'center',
          sortable: true,
          value: 'enabled',
        },
        {
          text: 'Actions',
          align: 'center',
          value: '',
        },
      ],
      editedIndex: -1,
      editedItem: {
        organization: '',
        repo: '',
        webhook: '',
        events: '',
        enabled: true,
      },
      defaultItem: {
        organization: '',
        repo: '',
        webhook: '',
        events: '',
        enabled: true,
      },
    };
  },
  computed: {
    formTitle() {
      return this.editedIndex === -1 ? 'New Connector' : 'Edit Item';
    },
  },
  watch: {
    dialog(val) {
      return val === true || this.close() === true;
    },
  },
  created() {
    this.initialize();
  },
  methods: {
    initialize() {
      fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks`,
      {credentials: "include",})
        .then((resp) => resp.json())
        .then((resp) => {
          this.hookList = resp;
        });
    },

    editItem(item) {
      this.editedIndex = this.hookList.indexOf(item);
      this.editedItem = item;
      this.dialog = true;
    },

    getHooks() {
      this.editedIndex = this.hookList.indexOf(item);
      this.editedItem = item;
      this.dialog = true;
    },

    deleteItem(item) {
      fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks/`+item.id, {
        method: 'delete',
        credentials: "include",
        headers: {
          'Content-Type': 'application/json',
        },
      })
        .then((result) => {
          if (!result.ok) {
            throw result;
          }
        })
        .then((response) => {
          this.message = 'Webhook deleted';
          this.alert = true;
          this.initialize();
          setTimeout(() => (this.alert = false), 5000);
        })
        .catch((result) => {
          this.isUpdating = false;
          this.errorAlert = true;
          result.text().then((body) => {
            this.errorMsg = body;
          });
          setTimeout(() => (this.errorAlert = false), 5000);
        });
    },

    close() {
      this.dialog = false;
      this.editedItem = this.defaultItem
      setTimeout(() => {
        this.editedIndex = -1;
        this.initialize();
      }, 300);
    },

    save() {
      const i = this.editedIndex;
      if (this.editedIndex > -1) {
        fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks/`+this.editedItem.id, {
          method: 'PUT',
          credentials: "include",
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.editedItem),
        })
          .then((result) => {
            if (!result.ok) {
              throw result;
            }
          })
          .then((response) => {
            this.message = 'Webhook updated';
            this.alert = true;
            this.initialize();
            setTimeout(() => (this.alert = false), 5000);
            this.editedItem = this.defaultItem
          })
          .catch((result) => {
            this.isUpdating = false;
            this.errorAlert = true;
            result.text().then((body) => {
              this.errorMsg = body;
            });
            setTimeout(() => (this.errorAlert = false), 5000);
            this.editedItem = this.defaultItem
          });
      } else {
        this.hookList.push(this.editedItem);

        fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks`, {
          method: 'post',
          credentials: "include",
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(this.editedItem),
        })
          .then((result) => {
            if (!result.ok) {
              throw result;
            }
          })
          .then((response) => {
            this.message = 'Webhook created';
            this.alert = true;
            this.initialize();
            setTimeout(() => (this.alert = false), 5000);
            this.editedItem = this.defaultItem
          })
          .catch((result) => {
            this.isUpdating = false;
            this.errorAlert = true;
            result.text().then((body) => {
              this.errorMsg = body;
            });
            setTimeout(() => (this.errorAlert = false), 5000);
          });
      }
      this.close();
    },
  },
};
</script>