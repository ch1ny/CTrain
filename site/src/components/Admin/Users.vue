<template>
	<div>
		<div class="head" style="margin-bottom: 10px">
			<el-pagination :total="total" layout="total, prev, pager, next, jumper" :current-page.sync="page" @current-change="changePage">
			</el-pagination>
		</div>
		<el-table :data="userList" v-loading="loading" stripe border>
			<el-table-column prop="id" label="身份证号"></el-table-column>
			<el-table-column prop="name" label="用户姓名"></el-table-column>
			<el-table-column label="管理权限">
				<template slot-scope="scope">
					<el-tag type="info" v-if="scope.row.admin"><i class="el-icon-medal-1"></i>管理员</el-tag>
					<el-button type="primary" @click="authorize(scope.row.id)" v-else><i class="el-icon-medal"></i>授权</el-button>
				</template>
			</el-table-column>
		</el-table>
	</div>
</template>

<script>
import $ajax from "../../ajax";
export default {
	data() {
		return {
			loading: true,
			userList: [],
			total: 0,
			page: 1,
			size: 10,
		};
	},
	methods: {
		changePage() {
			this.getUserList();
		},
		async getUserList() {
			this.loading = true;
			let res = await $ajax.post(
				"/admin/getUsers",
				{
					pageNum: this.page,
					pageSize: this.size,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.userList = res.data.data.users;
				this.total = res.data.data.total;
			}
			this.loading = false;
		},
		async authorize(userId) {
			this.loading = true;
			let res = await $ajax.post(
				"/admin/add",
				{
					id: userId,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.getUserList();
				this.$notify({
					title: "授权成功",
					message: "该用户已具备管理员权限",
					type: "success",
					duration: 1500,
				});
			} else {
				this.$notify({
					title: "授权失败",
					message: res.data.data,
					type: "error",
					duration: 1500,
				});
				this.loading = false;
			}
		},
	},
	created() {
		this.getUserList();
	},
};
</script>