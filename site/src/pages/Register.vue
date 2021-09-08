<template>
	<div>
		<el-switch class="switch" v-model="darkStyle" active-color="dimgray" active-icon-class="el-icon-moon" inactive-color="gold" inactive-icon-class="el-icon-sunny" @change="themeChange"></el-switch>
		<el-card class="register-card" :style="{'background': darkStyle?'rgba(10, 10, 10, 0.7)':'rgba(250, 250, 250, 0.7)'}">
			<div slot="header">
				<el-link @click="goBack" :style="{position:'absolute', left:'5%', color:darkStyle?'white':''}"><i class="el-icon-back"></i>后退</el-link>
				<span :style="{'color': darkStyle?'white':'dodgerblue'}">用户注册</span>
			</div>
			<el-form :model="formData" :rules="rules" ref="formData">
				<el-form-item prop="id">
					<el-input class="input" @keydown.enter.native="register" placeholder="请输入身份证号码" prefix-icon="iconfont icon-credentials_icon" v-model="formData.id"></el-input>
				</el-form-item>
				<el-form-item prop="email">
					<el-autocomplete class="input" placeholder="请输入邮箱" prefix-icon="el-icon-message" v-model="formData.email" :fetch-suggestions="emailSuggestions" :trigger-on-focus="false"></el-autocomplete>
				</el-form-item>
				<el-form-item prop="name">
					<el-input class="input" @keydown.enter.native="register" placeholder="请输入姓名" prefix-icon="el-icon-user" v-model="formData.name"></el-input>
				</el-form-item>
				<el-form-item prop="psw">
					<el-input class="input" @keydown.enter.native="register" placeholder="请输入密码" prefix-icon="el-icon-key" v-model="formData.psw" show-password></el-input>
				</el-form-item>
				<el-form-item prop="captcha">
					<el-input class="input" @keydown.enter.native="register" placeholder="请输入验证码" prefix-icon="el-icon-bell" v-model="formData.captcha" maxlength="6" show-word-limit></el-input>
				</el-form-item>
				<el-form-item>
					<el-button class="btn" :loading="isBuildingCaptcha" @click="sendCaptcha" :disabled="countdown != 60" plain>{{captcha}}</el-button>
					<el-button class="btn" @mouseover.native="mouseHover" @mouseleave.native="mouseLeave" @click="register" size="large" type="primary" :style="darkStyle?darkThemeBtnStyle:''" :loading="loading">注册</el-button>
				</el-form-item>
			</el-form>
		</el-card>
		<ribbons :darkStyle.sync="darkStyle" ref="ribbons" />
	</div>
</template>

<script>
import ribbons from "../components/Ribbons.vue";
import $ajax from "../ajax";

export default {
	components: { ribbons },
	name: "Register",
	data() {
		return {
			captcha: "获取验证码",
			countdown: 60,
			formData: {
				id: "",
				email: "",
				name: "",
				psw: "",
				captcha: "",
			},
			loading: false,
			isBuildingCaptcha: false,
			darkStyle: false,
			darkThemeBtnStyle:
				"background-color: gold; border-color: gold; color: #1f1f1f",
			rules: {
				id: [
					{
						required: true,
						message: "请输入身份证号",
						trigger: "blur",
					},
				],
				email: [
					{
						required: true,
						message: "请输入注册的邮箱",
						trigger: ["change", "blur"],
					},
					{
						pattern: /^[A-Za-z0-9]+@[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)+$/,
						message: "请输入合法的邮箱地址！",
						trigger: ["change", "blur"],
					},
				],
				name: [
					{ required: true, message: "请输入姓名", trigger: "blur" },
					{
						pattern: /^[\u4e00-\u9fa5]{2,20}$/,
						message: "请输入合法的中文姓名",
						trigger: ["blur", "change"],
					},
				],
				psw: [
					{ required: true, message: "请输入密码", trigger: "blur" },
				],
			},
			emails: [
				{ value: "qq.com" },
				{ value: "mail.sdu.edu.cn" },
				{ value: "126.com" },
				{ value: "163.com" },
				{ value: "gmail.com" },
				{ value: "outlook.com" },
			],
		};
	},
	methods: {
		mouseHover() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: yellow; border-color: yellow; color: #1f1f1f";
			}
		},
		mouseLeave() {
			if (this.darkStyle) {
				this.darkThemeBtnStyle =
					"background-color: gold; border-color: gold; color: #1f1f1f";
			}
		},
		register() {
			this.$refs["formData"].validate(async (valid) => {
				if (valid) {
					this.loading = true;
					let res = await $ajax.post("/user/register", {
						id: this.formData.id,
						email: this.formData.email,
						name: this.formData.name,
						psw: this.formData.psw,
						captcha: this.formData.captcha,
					});
					switch (res.data.code) {
						case 0:
							this.$notify({
								title: "注册成功",
								message: "注册成功！正在前往登录页面...",
								type: "success",
								duration: 1500,
								showClose: false,
								onClose: () => {
									this.$router.push({ path: "/login" });
								},
							});
							break;
						default:
							this.$notify({
								title: "注册失败",
								message: `注册失败，${res.data.result}`,
								type: "error",
								duration: 1500,
								showClose: false,
							});
							break;
					}
					this.loading = false;
				} else {
					console.log("error submit!!");
					return false;
				}
			});
		},
		async sendCaptcha() {
			if (this.formData.email == "") {
				this.$notify({
					title: "",
					message: "请输入邮箱！",
					type: "error",
					showClose: false,
					duration: 1000,
				});
			} else {
				let pattern = /^[A-Za-z0-9]+@[a-zA-Z0-9]+(\.[a-zA-Z0-9]+)+$/;
				if (pattern.test(this.formData.email)) {
					this.isBuildingCaptcha = true;
					this.captcha = "发送验证码";
					let res = await $ajax.post(
						"/user/sendRegisterCaptcha",
						{
							email: this.formData.email,
						},
						{
							headers: {
								Authorization:
									"Bearer " + localStorage.getItem("token"),
							},
						}
					);
					this.isBuildingCaptcha = false;
					if (res.data.code == 0) {
						this.$notify({
							title: "发送成功",
							message:
								"验证码发送成功，请前往注册邮箱中查看验证码",
							type: "success",
							duration: 3000,
						});
						this.captcha = "重新发送(" + this.countdown + "秒)";
						this.countdown--;
						let interval = window.setInterval(() => {
							this.captcha = "重新发送(" + this.countdown + "秒)";
							this.countdown--;
							if (this.countdown < 0) {
								this.captcha = "重新发送";
								this.countdown = 60;
								window.clearInterval(interval);
							}
						}, 1000);
					} else {
						this.$notify({
							title: "发送失败",
							message: "验证码发送失败",
							type: "error",
							duration: 3000,
						});
						this.captcha = "重新发送";
					}
				} else {
					this.$notify({
						title: "邮箱地址不合法！",
						message: "请输入合法的邮箱地址！",
						type: "error",
						showClose: false,
						duration: 1000,
					});
				}
			}
		},
		goBack() {
			this.$router.go(-1);
		},
		themeChange() {
			this.$nextTick(() => {
				this.$refs.ribbons.themeChange();
			});
		},
		// 邮箱自动补全
		emailSuggestions(queryString, callback) {
			let email = this.emails;
			let results = JSON.parse(JSON.stringify(email)); //把数组的浅复制换成深复制
			if (this.formData.email.indexOf("@") == -1) {
				for (let item in results) {
					results[item].value = queryString + "@" + email[item].value;
				}
			} else {
				let suffix = this.formData.email.substring(
					this.formData.email.indexOf("@") + 1,
					this.formData.email.length
				);
				for (let item in email) {
					if (email[item].value.indexOf(suffix) === 0) {
						results[item].value =
							this.formData.email.substring(
								0,
								this.formData.email.indexOf("@")
							) +
							"@" +
							email[item].value;
					} else {
						results[item].value = "";
					}
				}
			}
			callback(results);
		},
	},
	created() {
		if (localStorage.getItem("token") != null) {
			this.$router.replace("/");
		}
	},
};
</script>

<style scoped>
div {
	text-align: center;
}
.switch {
	position: absolute;
	right: 5%;
	top: 5%;
}
.input {
	width: 100%;
	margin-top: 5%;
}
.register-card {
	width: 30%;
	padding-bottom: 10px;
	position: absolute;
	left: 35%;
	top: 10%;
}
.btn {
	width: 30%;
	height: 50px;
	margin-block: 5%;
	margin-inline: 5%;
	text-align: center;
	padding: 1%;
}
</style>